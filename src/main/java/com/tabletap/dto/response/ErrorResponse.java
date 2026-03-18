package com.tabletap.dto.response;

import java.time.Instant;

public record ErrorResponse(String error, String code, Instant timestamp) {}
