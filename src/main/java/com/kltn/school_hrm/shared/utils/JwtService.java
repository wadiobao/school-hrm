package com.kltn.school_hrm.shared.utils;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.core.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey key;

	public JwtService(@Value("${app.security.jjwt.secret-key}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(User user) {
		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("role", user.getRole().getRoleCode())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 giờ
				.signWith(key)
				.compact();
	}

	public Claims verifyToken(String token) {
		return Jwts.parser().verifyWith(key).build()
				.parseSignedClaims(token).getPayload();
	}
}
