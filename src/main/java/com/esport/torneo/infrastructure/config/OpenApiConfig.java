package com.esport.torneo.infrastructure.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI/Swagger configuration for the Tournament Management System.
 * 
 * Provides comprehensive API documentation with security schemes,
 * server configurations, and detailed API information.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.name:Tournament Management System}")
    private String appName;

    @Value("${app.description:Sistema integral para gestión de torneos de e-sports}")
    private String appDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures the OpenAPI specification for the application.
     * 
     * @return OpenAPI configuration with security, servers, and API info
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * Creates API information including title, description, version, and contact details.
     */
    private Info apiInfo() {
        return new Info()
                .title(appName + " API")
                .description(appDescription + "\n\n" +
                        "## Características principales:\n" +
                        "- ✅ Gestión completa de torneos de e-sports\n" +
                        "- ✅ Sistema de tickets virtuales con QR\n" +
                        "- ✅ Autenticación JWT y OAuth2\n" +
                        "- ✅ Roles de usuario (ADMIN, ORGANIZER, USER)\n" +
                        "- ✅ Cache distribuido con Redis\n" +
                        "- ✅ Mensajería asíncrona con RabbitMQ\n" +
                        "- ✅ Arquitectura DDD y microservicios\n\n" +
                        "## Autenticación:\n" +
                        "La API utiliza JWT Bearer tokens. Para autenticarse:\n" +
                        "1. Obtener token del endpoint de login\n" +
                        "2. Incluir en header: `Authorization: Bearer <token>`\n\n" +
                        "## Roles y permisos:\n" +
                        "- **USER**: Ver torneos, comprar tickets\n" +
                        "- **ORGANIZER**: Crear y gestionar torneos\n" +
                        "- **ADMIN**: Acceso completo al sistema")
                .version(appVersion)
                .contact(contactInfo())
                .license(licenseInfo());
    }

    /**
     * Creates contact information for the API.
     */
    private Contact contactInfo() {
        return new Contact()
                .name("Andrés Orduz Grimaldo")
                .email("desarrollo@torneoesport.com")
                .url("https://github.com/andresorduz/TorneoAssessment");
    }

    /**
     * Creates license information for the API.
     */
    private License licenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configures the list of servers where the API is available.
     */
    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("https://api-dev.torneoesport.com")
                        .description("Development Environment"),
                new Server()
                        .url("https://api-staging.torneoesport.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.torneoesport.com")
                        .description("Production Environment")
        );
    }

    /**
     * Configures security components including JWT Bearer authentication.
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", jwtSecurityScheme())
                .addSecuritySchemes("oauth2", oauth2SecurityScheme());
    }

    /**
     * Configures JWT Bearer token security scheme.
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer token authentication. " +
                        "Obtener token del endpoint /api/v1/auth/login y usar en header: " +
                        "Authorization: Bearer <token>");
    }

    /**
     * Configures OAuth2 security scheme.
     */
    private SecurityScheme oauth2SecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth2 authentication with authorization code flow");
    }

    /**
     * Creates security requirement for protected endpoints.
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("bearerAuth");
    }
} 