package com.tabletap.websocket;

import com.tabletap.dto.response.OrderResponse;
import com.tabletap.dto.response.WaiterCallResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewOrder(OrderResponse order) {
        log.debug("Broadcasting new order {} to kitchen", order.id());
        messagingTemplate.convertAndSend("/topic/kitchen/orders/new", order);
    }

    public void broadcastOrderUpdate(OrderResponse order) {
        log.debug("Broadcasting order update {} status={}", order.id(), order.status());
        messagingTemplate.convertAndSend("/topic/kitchen/orders/updated", order);
        messagingTemplate.convertAndSend("/topic/orders/" + order.id(), order);
    }

    public void broadcastWaiterCall(WaiterCallResponse call) {
        log.debug("Broadcasting waiter call from table {}", call.table().number());
        messagingTemplate.convertAndSend("/topic/kitchen/waiter-calls", call);
    }
}
