package com.api.product.dto;

import jakarta.validation.constraints.NotNull;

public record ProductStockRequest(
        @NotNull(message = "É obrigatório informar a quantidade do ajuste de estoque")
        Integer quantity) {
}
