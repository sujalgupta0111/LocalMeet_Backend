package com.users.dtos;

import com.users.entity.Role;
import com.users.entity.UserStatus;

import lombok.Builder;

@Builder
public record CredentialsDTO(
    String username,
    String email,
    String password,
    String phone,
    Role role,
    UserStatus status
) {
}