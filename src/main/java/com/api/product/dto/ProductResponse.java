package com.api.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String description,
        Integer stock,
        BigDecimal price
        ) {
}
