package com.kltn.school_hrm.shared.configuration;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;
import com.kltn.school_hrm.shared.utils.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String authHeader = req.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			chain.doFilter(req, res);
			return;
		}
		try {
			Claims claims = jwtService.verifyToken(authHeader.substring(7));
			// CustomerPrincipal là record (Java 16+): record CustomerPrincipal(UUID
			// customerId) {}
			var principal = new CustomPrincipal(Long.valueOf(claims.getSubject()));
			// Mọi người dùng đã đăng nhập chỉ có quyền chung ROLE_USER; quyền theo từng quỹ
			// kiểm tra riêng (mục 6)
			var authorities = List.of(new SimpleGrantedAuthority(claims.get("role", String.class)));
//			log.info("Principal type: {}", principal.getClass().getName());
//			log.info("Principal value: {}", principal);
			var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (JwtException e) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		chain.doFilter(req, res);
	}
}
