package com.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        Integer id,
        @NotNull(message = "É obrigatório informar o status do produto, sendo true ou false")
        Boolean active,
        @NotNull(message = "É obrigatório informar a categoria do produto")
        Integer category,
        @NotBlank(message = "É obrigatório informar a descrição do produto")
        String description,
        @NotNull(message = "É obrigatório informar a quantidade em estoque do produto")
        Integer stock,
        @NotNull(message = "É obrigatório informar o valor do produto")
        BigDecimal price) {
}
