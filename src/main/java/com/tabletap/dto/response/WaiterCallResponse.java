package com.tabletap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WaiterCallResponse(
        UUID id,
        String message,
        TableInfo table,
        Instant createdAt
) {}
