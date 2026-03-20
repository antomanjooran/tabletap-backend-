package com.tabletap.dto.response;

import java.util.UUID;

public record CategorySimpleResponse(UUID id, String name, Integer sortOrder) {}