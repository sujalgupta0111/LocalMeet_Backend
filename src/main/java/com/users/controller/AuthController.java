package com.users.controller;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.users.config.security.jwt.JwtToken;
import com.users.config.security.jwt.UserDetailsImpl;
import com.users.dtos.request.LoginRequest;
import com.users.dtos.request.OAuthCodeExchangeRequest;
import com.users.dtos.request.UserDTO;
import com.users.dtos.response.ApiResponse;
import com.users.dtos.response.AuthResponse;
import com.users.config.security.oauth.OAuthCodeStore;

import com.users.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication APIs", description = "APIs for user registration and login")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final JwtToken jwtTokenUtil;
	private final OAuthCodeStore oauthCodeStore;

	@Operation(summary = "Login a user AND GIVE JWT TOKEN", description = "Authenticates a user and returns a JWT token along with user details")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		UserDetailsImpl userDetails = authService.login(request.identifier(), request.password());
		String token = jwtTokenUtil.generateToken(userDetails);

		AuthResponse authResponse = AuthResponse.builder().id(userDetails.getUser().getId()).token(token)
				.tokenType("Bearer").name(userDetails.getName()).username(userDetails.getUsername())
				.roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.email(userDetails.getUser().getCredentials().getEmail()).build();

		ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder().success(true)
				.message("User logged in successfully").data(authResponse).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.ok(response);
	}

	@PostMapping("/oauth/exchange")
	public ResponseEntity<ApiResponse<AuthResponse>> exchangeOAuthCode(
			@Valid @RequestBody OAuthCodeExchangeRequest request) {
		AuthResponse authResponse = oauthCodeStore.consume(request.code(), request.provider())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "OAuth code is invalid, expired, or already used"));

		ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder().success(true)
				.message("User logged in successfully").data(authResponse).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT token")
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserDTO user) {

		UserDetailsImpl savedUser = authService.register(user);

		String token = jwtTokenUtil.generateToken(savedUser);

		AuthResponse authResponse = AuthResponse.builder().id(savedUser.getUser().getId()).token(token)
				.tokenType("Bearer").name(savedUser.getName()).username(savedUser.getUsername())
				.roles(savedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.email(savedUser.getUser().getCredentials().getEmail()).build();

		ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder().success(true)
				.message("User registered successfully").data(authResponse).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}



}
