package com.api.exception;

public record ErrorResponse(
        int status,
        String message
) {
}
