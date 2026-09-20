package com.users.dtos;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record AuthResponse(
        UUID id,
        String token,
        String tokenType,
        String name,
        String username,
        String email,
        List<String> roles
) {
}