package com.tabletap.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record OrderItemRequest(
        @NotNull UUID menuItemId,
        @Positive int quantity,
        String notes
) {}
