package com.users.config.security.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.users.dtos.response.AuthResponse;

class OAuthCodeStoreTest {

	@Test
	void issuedCodeCanOnlyBeConsumedOnceByItsProvider() {
		OAuthCodeStore codeStore = new OAuthCodeStore();
		AuthResponse authResponse = AuthResponse.builder().token("test-token").build();

		String code = codeStore.issue("google", authResponse);
		assertNotEquals("", code);
		assertTrue(codeStore.consume(code, "discord").isEmpty());
		assertEquals(authResponse, codeStore.consume(code, "google").orElseThrow());
		assertTrue(codeStore.consume(code, "google").isEmpty());
	}
}
