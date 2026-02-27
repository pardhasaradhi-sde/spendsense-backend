package com.spendsense.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .servers(List.of(
                                                new Server().url("http://localhost:8080/api/v1")
                                                                .description("Development Server")))
                                .info(new Info()
                                                .title("SpendSense API")
                                                .version("1.0.0")
                                                .description("Expense tracking and budget management REST API")
                                                .contact(new Contact()
                                                                .name("Your Name")
                                                                .email("your.email@example.com")
                                                                .url("https://github.com/yourusername/spendsense"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .name("Authorization")
                                                                .description("JWT token from Clerk authentication")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
}
