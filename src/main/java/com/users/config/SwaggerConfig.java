package com.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration

public class SwaggerConfig {
	@Bean
	OpenAPI customOpenAPI() {

		return new OpenAPI()
				.info(new Info()
				.title("User Service API")
				.description("API Documentation for User Service System")
				.version("1.0")
				.contact(
						new Contact()
						.name("Sujal Gupta")
						.email("sujal@gmail.com")
						)
				);
	}
}
