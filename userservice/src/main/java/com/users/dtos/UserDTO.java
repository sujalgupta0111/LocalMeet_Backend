package com.users.dtos;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;


@Builder
public record UserDTO(
    UUID id,
    String firstName,
    String lastName,
    CredentialsDTO credentials,
    AddressDTO address
) {
}