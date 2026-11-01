package com.users.dtos.request;



import com.users.enums.Role;
import com.users.enums.UserStatus;

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