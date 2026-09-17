package com.api.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotNull(message = "É obrigatório informar a categoria do produto")
        Integer category,
        @NotBlank(message = "É obrigatório informar a descrição do produto")
        String description,
        @NotNull(message = "É obrigatório informar a quantidade em estoque do produto")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer stock,
        @NotNull(message = "É obrigatório informar o valor do produto")
        @Positive(message = "O valor do produto deve ser maior que zero")
        @Digits(
                integer = 10,
                fraction = 2,
                message = "O valor do produto deve ter no máximo 10 dígitos inteiros e 2 casas decimais"
        )
        BigDecimal price) {
}
