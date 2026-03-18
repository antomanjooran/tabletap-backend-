package com.tabletap.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String code;
    private final int    statusCode;

    public ApiException(String message, String code, int statusCode) {
        super(message);
        this.code       = code;
        this.statusCode = statusCode;
    }
}
