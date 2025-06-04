package com.esport.torneo.config;

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
 * Configuración de OpenAPI/Swagger para documentación de la API.
 * 
 * Define la configuración de Swagger UI y la documentación
 * automática de los endpoints de la API REST.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configuración principal de OpenAPI.
     * 
     * @return configuración de OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .addServersItem(new Server().url("http://localhost:8080").description("Servidor de desarrollo"))
            .addServersItem(new Server().url("https://api.torneoesport.com").description("Servidor de producción"))
            .components(securityComponents())
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Información general de la API.
     * 
     * @return información de la API
     */
    private Info apiInfo() {
        return new Info()
            .title("TorneoAssessment API")
            .description("""
                ## Sistema de Gestión de Torneos E-Sport
                
                API REST para la gestión integral de torneos de videojuegos que incluye:
                
                ### Funcionalidades principales:
                - 🎮 **Gestión de Juegos y Categorías**: CRUD completo de videojuegos y categorías
                - 🏆 **Administración de Torneos**: Creación, gestión y seguimiento de torneos
                - 🎫 **Sistema de Tickets Virtuales**: Generación de tickets con códigos QR únicos
                - 👥 **Gestión de Participantes**: Registro y administración de participantes
                - 💰 **Cálculo de Comisiones**: Sistema automático de comisiones por servicios
                - 📊 **Estadísticas y Reportes**: Dashboards con métricas en tiempo real
                
                ### Seguridad:
                - Autenticación OAuth2/JWT
                - Autorización basada en roles (ADMIN, ORGANIZER, USER)
                - Validación de entrada y sanitización
                
                ### Tecnologías:
                - Spring Boot 3.x, Spring Security, Spring Data JPA
                - PostgreSQL, Redis (Cache), RabbitMQ
                - Docker, Kubernetes ready
                """)
            .version("1.0.0")
            .contact(apiContact())
            .license(apiLicense());
    }

    /**
     * Información de contacto.
     * 
     * @return datos de contacto
     */
    private Contact apiContact() {
        return new Contact()
            .name("Andrés Orduz Grimaldo")
            .email("desarrollo@torneoesport.com")
            .url("https://github.com/andresorduz/TorneoAssessment");
    }

    /**
     * Información de licencia.
     * 
     * @return información de licencia
     */
    private License apiLicense() {
        return new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Componentes de seguridad para la documentación.
     * 
     * @return componentes de seguridad configurados
     */
    private Components securityComponents() {
        return new Components()
            .addSecuritySchemes("bearerAuth", 
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("""
                        Token JWT para autenticación. 
                        
                        **Formato**: `Bearer {token}`
                        
                        **Obtención**: A través del sistema de autenticación OAuth2.
                        
                        **Roles disponibles**:
                        - `ADMIN`: Acceso completo al sistema
                        - `ORGANIZER`: Gestión de torneos y participantes  
                        - `USER`: Consulta y participación en torneos
                        """)
            );
    }
} 