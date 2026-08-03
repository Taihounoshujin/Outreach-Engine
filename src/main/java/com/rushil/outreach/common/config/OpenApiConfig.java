package com.rushil.outreach.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration.
 * <p>
 * Access the UI at http://localhost:8080/swagger-ui.html
 * Raw OpenAPI JSON at  http://localhost:8080/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TaskFlow API",
                version = "1.0",
                description = "REST API for a Trello-style project management application. "
                        + "Supports users, workspaces, boards, columns, and cards. "
                        + "Authentication via JWT — obtain a token from POST /api/auth/login.",
                contact = @Contact(name = "Rushil", url = "https://github.com/Taihounoshujin/Taskflow-Application")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste your JWT here (without the 'Bearer ' prefix — Swagger adds it automatically)"
)
public class OpenApiConfig {
    // No methods needed — the annotations do everything.
}