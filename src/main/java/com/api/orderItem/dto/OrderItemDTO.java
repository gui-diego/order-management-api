package com.api.orderItem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDTO(
        @NotNull(message = "É obrigatório informar o ID do produto")
        Integer productId,
        @NotNull(message = "É obrigatório informar a quantidade de unidades do produto")
        @Positive(message = "A quantidade de unidades deve ser maior que zero")
        Integer quantity) {
}
