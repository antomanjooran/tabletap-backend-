package com.tabletap.dto.response;

public record PaymentIntentResponse(
        String clientSecret,
        long amountPence,
        String currency
) {}
