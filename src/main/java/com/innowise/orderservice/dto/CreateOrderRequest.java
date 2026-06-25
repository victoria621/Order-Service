package com.innowise.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "User ID cannot be null")
        Long userId,
        @NotNull(message = "Items cannot be null")
        @Valid
        List<OrderItemRequest> items
) {
}
