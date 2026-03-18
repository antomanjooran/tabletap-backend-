package com.tabletap.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String notes,
        MenuItemInfo menuItem
) {}
