package com.tabletap.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String status,
        String notes,
        BigDecimal subtotal,
        BigDecimal serviceCharge,
        BigDecimal total,
        String paymentMethod,
        String paymentStatus,
        TableInfo table,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
