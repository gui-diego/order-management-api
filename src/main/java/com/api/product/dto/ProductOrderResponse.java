package com.api.product.dto;

import java.math.BigDecimal;

public record ProductOrderResponse(
        Integer id,
        String description,
        BigDecimal price) {
}
