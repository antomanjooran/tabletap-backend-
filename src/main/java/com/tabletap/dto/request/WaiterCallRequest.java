package com.tabletap.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WaiterCallRequest(
        @NotNull UUID tableId,
        String message
) {}
