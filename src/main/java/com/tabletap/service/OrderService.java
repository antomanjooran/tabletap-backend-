package com.tabletap.service;

import com.tabletap.dto.request.PlaceOrderRequest;
import com.tabletap.dto.response.*;
import com.tabletap.entity.*;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.*;
import com.tabletap.websocket.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository    orderRepository;
    private final TableRepository    tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final RealtimeService    realtimeService;

    private static final Map<Order.Status, Set<Order.Status>> TRANSITIONS = Map.of(
            Order.Status.PENDING,   EnumSet.of(Order.Status.CONFIRMED, Order.Status.PREPARING, Order.Status.CANCELLED),
            Order.Status.CONFIRMED, EnumSet.of(Order.Status.CONFIRMED, Order.Status.PREPARING, Order.Status.CANCELLED),
            Order.Status.PREPARING, EnumSet.of(Order.Status.READY, Order.Status.CANCELLED),
            Order.Status.READY,     EnumSet.of(Order.Status.SERVED),
            Order.Status.SERVED,    EnumSet.noneOf(Order.Status.class),
            Order.Status.CANCELLED, EnumSet.noneOf(Order.Status.class)
    );

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        RestaurantTable table = tableRepository.findById(request.tableId())
                .filter(RestaurantTable::getIsActive)
                .orElseThrow(() -> new ApiException("Table not found", "TABLE_NOT_FOUND", 404));

        List<UUID> menuItemIds = request.items().stream().map(i -> i.menuItemId()).toList();
        List<MenuItem> menuItems = menuItemRepository.findByIdInAndIsAvailableTrue(menuItemIds);

        if (menuItems.size() != menuItemIds.size()) {
            Set<UUID> foundIds = menuItems.stream().map(MenuItem::getId).collect(Collectors.toSet());
            menuItemIds.stream().filter(id -> !foundIds.contains(id)).findFirst()
                    .ifPresent(id -> { throw new ApiException("Menu item " + id + " is unavailable", "ITEM_UNAVAILABLE", 422); });
        }

        Map<UUID, MenuItem> itemMap = menuItems.stream().collect(Collectors.toMap(MenuItem::getId, m -> m));

        String pm = request.paymentMethod() != null
                ? request.paymentMethod().toUpperCase() : null;

        Order order = Order.builder().table(table).notes(request.notes())
                .paymentMethod(pm).status(Order.Status.PENDING).build();

        request.items().forEach(req -> {
            MenuItem mi = itemMap.get(req.menuItemId());
            order.addItem(OrderItem.builder().menuItem(mi).quantity(req.quantity())
                    .unitPrice(mi.getPrice()).notes(req.notes()).build());
        });

        Order saved = orderRepository.save(order);
        log.info("Order placed: {} for table {}", saved.getId(), table.getNumber());

        Order withTotals = orderRepository.findByIdWithDetails(saved.getId()).orElseThrow();
        OrderResponse response = toResponse(withTotals);
        realtimeService.broadcastNewOrder(response);
        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return orderRepository.findByIdWithDetails(orderId).map(this::toResponse)
                .orElseThrow(() -> new ApiException("Order not found", "ORDER_NOT_FOUND", 404));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrders() {
        return orderRepository.findActiveOrdersWithDetails().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, Order.Status newStatus) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ApiException("Order not found", "ORDER_NOT_FOUND", 404));

        Set<Order.Status> allowed = TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new ApiException("Invalid transition: " + order.getStatus() + " to " + newStatus,
                    "INVALID_TRANSITION", 422);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order {} status changed to {}", orderId, newStatus);

        OrderResponse response = toResponse(updated);
        realtimeService.broadcastOrderUpdate(response);
        return response;
    }

    @Transactional(readOnly = true)
    public DashboardStats getTodayStats() {
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant end   = start.plus(1, ChronoUnit.DAYS);
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        BigDecimal revenue = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.SERVED)
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStats(
                orders.size(),
                orders.stream().filter(o -> o.getStatus() == Order.Status.SERVED).count(),
                orders.stream().filter(o -> o.getStatus() == Order.Status.PENDING).count(),
                orders.stream().filter(o -> o.getStatus() == Order.Status.PREPARING).count(),
                orders.stream().filter(o -> o.getStatus() == Order.Status.READY).count(),
                revenue
        );
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getId(), i.getQuantity(), i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())), i.getNotes(),
                        new MenuItemInfo(i.getMenuItem().getId(), i.getMenuItem().getName(), i.getMenuItem().getEmoji())))
                .toList();

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getNotes(),
                order.getSubtotal(), order.getServiceCharge(), order.getTotal(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                new TableInfo(order.getTable().getId(), order.getTable().getNumber(), order.getTable().getName()),
                items, order.getCreatedAt(), order.getUpdatedAt());
    }
}