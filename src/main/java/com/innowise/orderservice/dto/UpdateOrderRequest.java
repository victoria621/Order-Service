package com.innowise.orderservice.dto;

import com.innowise.orderservice.entity.OrderStatus;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateOrderRequest(
        OrderStatus status,
        @Valid
        List<OrderItemResponse> items
) {
}
