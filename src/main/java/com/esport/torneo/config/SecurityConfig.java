package com.esport.torneo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad para la aplicación.
 * 
 * Define las reglas de seguridad, autenticación y autorización
 * para los endpoints de la API REST.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/categories/**",
        "/api/v1/games/**",
        "/api/v1/tickets/validate/**",
        "/api/v1/tournaments/*/public",
        "/api/v1/tournaments/search/**",
        "/api/v1/tournaments/category/**",
        "/api/v1/tournaments/game/**",
        "/actuator/health",
        "/actuator/info",
        "/actuator/metrics",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    private static final String[] ADMIN_ENDPOINTS = {
        "/api/v1/tournaments/*/approve",
        "/api/v1/tournaments/*/reject",
        "/api/v1/categories/*/reactivate",
        "/api/v1/games/*/reactivate",
        "/api/v1/tickets/process-expired",
        "/api/v1/**/stats",
        "/actuator/**"
    };

    /**
     * Configuración principal de seguridad.
     * 
     * @param http configurador de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Deshabilitar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar manejo de sesiones (stateless para JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurar autorización de requests
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // Endpoints de administración (solo ADMIN)
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                
                // Operaciones de lectura (GET) - usuarios autenticados
                .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                
                // Operaciones de tickets - usuarios autenticados
                .requestMatchers(HttpMethod.POST, "/api/v1/tickets/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**").authenticated()
                
                // Operaciones de torneos - ORGANIZER o ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/tournaments/**")
                    .hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tournaments/**")
                    .hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tournaments/**")
                    .hasAnyRole("ORGANIZER", "ADMIN")
                
                // Operaciones de categorías y juegos - solo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/v1/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/games/**").hasRole("ADMIN")
                
                // Cualquier otra request requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar OAuth2 Resource Server (JWT) - configuración básica
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                )
            )
            
            .build();
    }

    /**
     * Configuración de CORS para permitir requests desde frontend.
     * 
     * @return fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes específicos (ajustar en producción)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://*.torneoesport.com",
            "https://*.vercel.app"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Codificador de contraseñas BCrypt.
     * 
     * @return codificador de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
} 