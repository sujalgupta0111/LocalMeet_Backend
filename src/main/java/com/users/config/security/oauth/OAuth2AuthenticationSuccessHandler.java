package com.users.config.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.users.config.security.jwt.JwtToken;
import com.users.config.security.jwt.UserDetailsImpl;
import com.users.dtos.request.OAuth2UserDetailsDTO;
import com.users.dtos.response.AuthResponse;
import com.users.entities.Credentials;
import com.users.entities.User;
import com.users.enums.RegistrationSource;
import com.users.service.AuthService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler
		implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	public static int Counter = 0;
	private final JwtToken jwtTokenUtil;
	private final OAuthCodeStore oauthCodeStore;
	private final ObjectMapper objectMapper;

	@Value("${app.frontend-url}")
	private String frontendOrigin;

	@Autowired
	@Lazy
	private AuthService authService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		OAuth2User oauth2User = oauthToken.getPrincipal();

		Map<String, Object> attributes = oauth2User.getAttributes();
		String provider = oauthToken.getAuthorizedClientRegistrationId();

		try {
			OAuth2UserDetailsDTO oauth2UserDetailsDTO = saveOAuthUser(provider, attributes);
			User user = OAuth2UserDetailsDTOToUser(oauth2UserDetailsDTO);
			UserDetailsImpl userDetails = authService.registerOuth(user);
			String token = jwtTokenUtil.generateToken(userDetails);
			AuthResponse authResponse = AuthResponse.builder().id(userDetails.getUser().getId()).token(token)
					.tokenType("Bearer").name(userDetails.getName()).username(userDetails.getUsername())
					.roles(userDetails.getAuthorities().stream().map(authority -> authority.getAuthority()).toList())
					.email(userDetails.getUser().getCredentials().getEmail()).build();
			String code = oauthCodeStore.issue(provider, authResponse);

			writePopupResponse(response, "success", provider, code, "Sign-in successful.");
		} catch (RuntimeException exception) {
			log.error("OAuth account processing failed for provider {}", provider, exception);
			writePopupResponse(response, "error", provider, null, "Sign-in could not be completed.");
		}
	}

	@Override
	public void onAuthenticationFailure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        @NonNull AuthenticationException exception) throws IOException, ServletException {
		writePopupResponse(response, "error", getProviderFromRequest(request), null,
				"Sign-in was cancelled or failed.");
	}

	private void writePopupResponse(HttpServletResponse response, String status, String provider, String code,
			String message) throws IOException {
		Map<String, String> payload = new HashMap<>();
		payload.put("type", "oauth:result");
		payload.put("status", status);
		payload.put("provider", provider);
		payload.put("message", message);
		if (code != null) {
			payload.put("code", code);
		}
		String serializedPayload = serialize(payload);
		String serializedOrigin = serialize(frontendOrigin);
		String nonce = UUID.randomUUID().toString();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Content-Security-Policy",
				"default-src 'none'; script-src 'nonce-" + nonce + "'; frame-ancestors 'none'; base-uri 'none'");

		response.getWriter().write(
				"<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
						+ "<title>Sign-in complete</title></head><body>"
						+ "<p>Completing sign-in...</p><script nonce=\"" + nonce + "\">"
						+ "if (window.opener) { window.opener.postMessage(" + serializedPayload + ", "
						+ serializedOrigin + "); } window.close();"
						+ "</script></body></html>");
	}

	private String serialize(Object value) throws IOException {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException exception) {
			throw new IOException("Unable to create the OAuth popup response.", exception);
		}
	}

	private String getProviderFromRequest(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		String callbackPrefix = request.getContextPath() + "/login/oauth2/code/";
		if (!requestUri.startsWith(callbackPrefix)) {
			return "unknown";
		}

		String provider = requestUri.substring(callbackPrefix.length());
		return Set.of("google", "discord", "github", "facebook").contains(provider) ? provider : "unknown";
	}

	private User OAuth2UserDetailsDTOToUser(OAuth2UserDetailsDTO userDetailsDTO) {

		// Credentials object zaroori hai kyunki aapka AuthService isme se email nikalta
		// hai: user.getCredentials().getEmail()
		Credentials credentials = Credentials.builder().username(userDetailsDTO.getEmail())
				.email(userDetailsDTO.getEmail()).password(UUID.randomUUID().toString()) // Random password for OAuth
				.isEmailVerified(userDetailsDTO.getEmailVerified() != null ? userDetailsDTO.getEmailVerified() : true)
				.build();

		String firstName = userDetailsDTO.getFirstName() != null ? userDetailsDTO.getFirstName()
				: userDetailsDTO.getName();

		User user = User.builder().firstName(firstName).lastName(userDetailsDTO.getLastName())
				.registrationSource(RegistrationSource.valueOf(userDetailsDTO.getProvider().toUpperCase()))
				.credentials(credentials) // Link Credentials to User
				.build();

		return user;
	}

	private OAuth2UserDetailsDTO saveOAuthUser(String provider, Map<String, Object> attributes) {

		OAuth2UserDetailsDTO.OAuth2UserDetailsDTOBuilder builder = OAuth2UserDetailsDTO.builder().provider(provider);

		switch (provider.toLowerCase()) {

		case "google" -> {
			builder.providerUserId((String) attributes.get("sub")).username((String) attributes.get("email"))
					.email((String) attributes.get("email")).name((String) attributes.get("name"))
					.firstName((String) attributes.get("given_name")).lastName((String) attributes.get("family_name"))
					.profileImageUrl((String) attributes.get("picture"))
					.emailVerified((Boolean) attributes.get("email_verified"))
					.locale((String) attributes.get("locale"));
		}

		case "discord" -> {
			builder.providerUserId((String) attributes.get("id")).username((String) attributes.get("username"))
					.email((String) attributes.get("email")).name((String) attributes.get("global_name"))
					.profileImageUrl((String) attributes.get("avatar"))
					.emailVerified((Boolean) attributes.get("verified")).locale((String) attributes.get("locale"));
		}

		case "github" -> {
			builder.providerUserId(String.valueOf(attributes.get("id"))).username((String) attributes.get("login"))
					.email((String) attributes.get("email")).name((String) attributes.get("name"))
					.profileImageUrl((String) attributes.get("avatar_url"))
					.profileUrl((String) attributes.get("html_url"));
		}

		case "facebook" -> {
			builder.providerUserId((String) attributes.get("id")).name((String) attributes.get("name"))
					.email((String) attributes.get("email"));
		}

		default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
		}

		return builder.build();
	}
}