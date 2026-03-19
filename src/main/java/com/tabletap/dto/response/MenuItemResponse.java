package com.tabletap.dto.response;
import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(UUID id, String name, String description, BigDecimal price,
                               String emoji, String imageUrl, boolean isAvailable, Integer quantityAvailable, int sortOrder) {}