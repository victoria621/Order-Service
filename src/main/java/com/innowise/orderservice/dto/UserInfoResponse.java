package com.innowise.orderservice.dto;

public record UserInfoResponse(
        Long id,
        String email,
        String name,
        String surname,
        boolean active
) {
}
