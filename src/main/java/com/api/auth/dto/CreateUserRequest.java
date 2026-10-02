package com.api.auth.dto;

import com.api.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "É obrigatório informar o nome")
        String name,
        @NotBlank(message = "É obrigatório informar o e-mail")
        @Email(message = "O e-mail informado é inválido")
        String email,
        @NotBlank(message = "É obrigatório informar a senha")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,
        @NotNull(message = "É obrigatório informar o papel do usuário")
        Role role) {
}
