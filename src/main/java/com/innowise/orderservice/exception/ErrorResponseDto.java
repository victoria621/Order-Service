package com.innowise.orderservice.exception;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String message,
        String errorMessage,
        LocalDateTime errorTime
) {
}
