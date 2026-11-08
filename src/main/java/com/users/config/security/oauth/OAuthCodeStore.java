package com.users.config.security.oauth;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.users.dtos.response.AuthResponse;

@Component
public class OAuthCodeStore {

	private static final long CODE_LIFETIME_SECONDS = 120;
	private final SecureRandom secureRandom = new SecureRandom();
	private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();

	public String issue(String provider, AuthResponse authResponse) {
		removeExpiredCodes();

		byte[] randomCode = new byte[32];
		secureRandom.nextBytes(randomCode);
		String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomCode);
		codes.put(code, new CodeEntry(provider, authResponse, Instant.now().plus(CODE_LIFETIME_SECONDS, ChronoUnit.SECONDS)));

		return code;
	}

	public Optional<AuthResponse> consume(String code, String provider) {
		if (code == null || provider == null) {
			return Optional.empty();
		}

		CodeEntry entry = codes.get(code);
		if (entry == null || !entry.provider().equalsIgnoreCase(provider)) {
			return Optional.empty();
		}

		if (!entry.expiresAt().isAfter(Instant.now())) {
			codes.remove(code, entry);
			return Optional.empty();
		}

		return codes.remove(code, entry) ? Optional.of(entry.authResponse()) : Optional.empty();
	}

	private void removeExpiredCodes() {
		Instant now = Instant.now();
		codes.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
	}

	private record CodeEntry(String provider, AuthResponse authResponse, Instant expiresAt) {
	}
}
