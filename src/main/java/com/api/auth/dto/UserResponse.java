package com.api.auth.dto;

import com.api.user.enums.Role;

public record UserResponse(Integer id, String name, String email, Role role) {
}
