package com.innowise.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long itemId,
        String itemName,
        BigDecimal itemPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
