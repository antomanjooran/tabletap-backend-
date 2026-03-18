package com.tabletap.dto.response;

import java.util.List;
import java.util.UUID;

public record MenuResponse(
        UUID id,
        String name,
        int sortOrder,
        List<MenuItemResponse> items
) {}
