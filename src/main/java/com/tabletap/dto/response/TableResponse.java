package com.tabletap.dto.response;

import java.util.UUID;

public record TableResponse(UUID id, int number, String name, int capacity) {}
