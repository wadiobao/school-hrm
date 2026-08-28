package com.kltn.school_hrm.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable()) // Tắt CSRF đối với REST API
	        .authorizeHttpRequests(auth -> auth
	            // Cho phép truy cập không cần token cho các endpoint này và web frontend
	            .requestMatchers("/api/v1/users/register", "/api/v1/auth/login", 
	                             "/", "/employees", "/css/**", "/js/**", "/images/**").permitAll()
	            // Tất cả API khác bắt buộc phải đăng nhập
	            .anyRequest().authenticated()
	        );
	    return http.build();
	}

}
