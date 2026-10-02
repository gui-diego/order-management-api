package com.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "É obrigatório informar o nome")
        String name,
        @NotBlank(message = "É obrigatório informar o e-mail")
        @Email(message = "O e-mail informado é inválido")
        String email,
        @NotBlank(message = "É obrigatório informar a senha")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password) {
}
