package com.api.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        Integer categoryId,
        String description,
        Integer stock,
        BigDecimal price,
        boolean active) {
}
