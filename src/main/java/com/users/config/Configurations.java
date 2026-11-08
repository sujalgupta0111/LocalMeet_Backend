
package com.users.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central application configuration class.
 *
 * Provides configuration for:
 * 1. JPA auditing.
 * 2. Jackson ObjectMapper.
 * 3. Swagger/OpenAPI documentation.
 * 4. Virtual thread executor.
 * 5. Platform thread pool executor.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class Configurations {

	/**
	 * Provides the current auditor for JPA auditing.
	 *
	 * Currently, all database changes are attributed to SYSTEM.
	 * This can later be replaced with the authenticated user's
	 * identity from Spring Security's SecurityContext.
	 *
	 * @return AuditorAware containing the current auditor.
	 */
	@Bean(name = "auditorAware")
	public AuditorAware<String> auditorAware() {

		return () -> Optional.of("SYSTEM");
	}
	/**
	 * Configures Jackson ObjectMapper for JSON serialization
	 * and deserialization throughout the application.
	 *
	 * @return configured ObjectMapper instance.
	 */
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	/**
	 * Configures Swagger/OpenAPI documentation.
	 *
	 * Defines application API metadata, including title,
	 * description, version, and contact information.
	 *
	 * @return OpenAPI configuration.
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("User Service API")
						.description("API Documentation for User Service System")
						.version("1.0")
						.contact(new Contact()
								.name("Sujal Gupta")
								.email("sujal@gmail.com")
						)
				);
	}

	/**
	 * Creates an executor that uses Java virtual threads.
	 *
	 * Virtual threads are lightweight threads suitable for
	 * I/O-intensive and blocking operations.
	 *
	 * @return ExecutorService backed by virtual threads.
	 */
	@Bean(name = "virtualThreadExecutor", destroyMethod = "close")
	public ExecutorService virtualThreadExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	/**
	 * Creates a fixed-size platform thread pool executor.

	 * Suitable for CPU-intensive operations and tasks
	 * requiring controlled thread concurrency.+

	 * @return configured platform thread pool executor.
	 */
	@Bean(name = "platformThreadExecutor")
	public ThreadPoolTaskExecutor platformThreadExecutor() {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("platform-task-");

		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);

		executor.initialize();

		return executor;
	}
}
