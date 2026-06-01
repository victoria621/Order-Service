package com.innowise.orderservice.dto;

import java.math.BigDecimal;

public record ItemResponse(
        Long id,
        String name,
        BigDecimal price
) {
}
