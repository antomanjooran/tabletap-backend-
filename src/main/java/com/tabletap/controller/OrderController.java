package com.tabletap.controller;
import com.tabletap.dto.request.PlaceOrderRequest;
import com.tabletap.dto.response.*;
import com.tabletap.entity.Order;
import com.tabletap.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getOrdersByDate(date != null ? date : LocalDate.now()));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<DashboardStats> getTodayStats() {
        return ResponseEntity.ok(orderService.getTodayStats());
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<List<Map<String, Object>>> getRevenue(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(orderService.getRevenueByDate(days));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(orderService.updateStatus(id, Order.Status.valueOf(statusStr.toUpperCase())));
    }
}