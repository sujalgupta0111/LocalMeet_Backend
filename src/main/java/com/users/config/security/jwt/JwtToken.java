package com.users.config.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

@Component
public class JwtToken {

	@Value("${jwt.token.validity}")
	private long jwtValidity;

	@Value("${jwt.secret}")
	private String secretKey;

	@PostConstruct
	public void validateConfiguration() {
		if (secretKey == null || secretKey.trim().isEmpty()) {
			throw new IllegalStateException("JWT secret key must not be null or empty. Please configure 'jwt.secret' via JWT_SECRET in .env or environment variables.");
		}
		if (secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("JWT secret key must be at least 256 bits (32 bytes) long for secure HMAC signing.");
		}
		if (jwtValidity <= 0) {
			throw new IllegalStateException("JWT token validity must be a positive number of milliseconds. Current value: " + jwtValidity);
		}
	}

	public long getJwtValidity() {
		return jwtValidity;
	}

	public String generateToken(UserDetails userDetails) {

		List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

		Date now = new Date();
		Date expiration = new Date(now.getTime() + jwtValidity);

		return Jwts.builder().subject(userDetails.getUsername()).claim("roles", roles).issuedAt(now)
				.expiration(expiration).signWith(getKey()).compact();
	}

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	public Claims getClaims(String token) {

		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}

	public String getUsernameFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) {

		try {
			Claims claims = getClaims(token);

			return claims.getSubject().equals(userDetails.getUsername());

		} catch (Exception e) {
			return false;
		}
	}
}