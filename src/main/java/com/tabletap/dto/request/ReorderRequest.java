package com.tabletap.dto.request;

import java.util.UUID;

public record ReorderRequest(UUID id, Integer sortOrder) {}