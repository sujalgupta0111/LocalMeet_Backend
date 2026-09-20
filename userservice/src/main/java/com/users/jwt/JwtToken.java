package com.users.jwt;

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

@Component
public class JwtToken {

	@Value("${jwt.token.validity}")
	private long jwtValidity;

	@Value("${jwt.secret}")
	private String secretKey;

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