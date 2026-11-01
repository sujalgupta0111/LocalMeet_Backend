package com.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration class to enable JPA auditing for automatic tracking of
 * entity creation and modification timestamps.
 * 
 * This configuration enables:
 * - Automatic population of @CreatedDate fields
 * - Automatic updating of @LastModifiedDate fields
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaAuditingConfig {
	
	/**
	 * Provides the current auditor information (user making the change).
	 * This can be extended to fetch from SecurityContext or other sources.
	 * 
	 * @return AuditorAware implementation
	 */
	@Bean(name = "auditorAware")
	public AuditorAware<String> auditorAware() {
		return () -> Optional.of("SYSTEM");
	}

}
