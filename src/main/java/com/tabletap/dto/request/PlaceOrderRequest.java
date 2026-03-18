package com.tabletap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID tableId,
        @NotNull @NotEmpty @Valid List<OrderItemRequest> items,
        String notes,
        String paymentMethod
) {}
