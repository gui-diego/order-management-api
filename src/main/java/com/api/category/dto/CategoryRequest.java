package com.api.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "É obrigatório informar o nome da categoria")
        String name) {
}
