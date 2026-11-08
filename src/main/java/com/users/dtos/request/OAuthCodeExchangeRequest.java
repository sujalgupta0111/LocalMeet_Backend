package com.users.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeExchangeRequest(
		@NotBlank String code,
		@NotBlank String provider) {
}
