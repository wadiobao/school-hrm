package com.kltn.school_hrm.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    // Cấu hình để tự động lấy username của người đang login qua Spring Security (nếu cần)
}