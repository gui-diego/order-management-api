package com.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "É obrigatório informar o e-mail")
        @Email(message = "O e-mail informado é inválido")
        String email,
        @NotBlank(message = "É obrigatório informar a senha")
        String password) {
}
