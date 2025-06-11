package com.esport.torneo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * Configuración de seguridad para la aplicación.
 * 
 * Implementa:
 * - Autenticación JWT
 * - Autorización basada en roles
 * - CORS configuration
 * - Endpoints públicos y protegidos
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configuración principal de seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos - sin autenticación
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                
                // Documentación API - público
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Endpoints de consulta - requieren autenticación básica
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/active").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/games/active").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/games/popular").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/tournaments/public").authenticated()
                
                // Gestión de categorías - solo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasRole("ADMIN")
                
                // Gestión de juegos - solo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/games").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/games/**").hasRole("ADMIN")
                
                // Gestión de usuarios - ADMIN y operaciones propias
                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/profile").authenticated()
                
                // Gestión de torneos - ORGANIZER y ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/tournaments").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tournaments/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tournaments/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tournaments/**/publish").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tournaments/**/cancel").hasAnyRole("ORGANIZER", "ADMIN")
                
                // Participación en torneos - usuarios autenticados
                .requestMatchers(HttpMethod.POST, "/api/v1/tournaments/**/participants").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tournaments/**/participants/**").authenticated()
                
                // Gestión de tickets - usuarios autenticados
                .requestMatchers(HttpMethod.POST, "/api/v1/tickets").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/tickets/my-tickets").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/tickets/**/validate").hasAnyRole("ORGANIZER", "ADMIN")
                
                // Estadísticas y reportes - ADMIN
                .requestMatchers("/api/v1/**/stats").hasRole("ADMIN")
                .requestMatchers("/api/v1/reports/**").hasRole("ADMIN")
                
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
            )
            .build();
    }

    /**
     * Decodificador JWT.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        var secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * Convertidor de autenticación JWT para extraer roles.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        authenticationConverter.setPrincipalClaimName("sub");

        return authenticationConverter;
    }

    /**
     * Encoder de contraseñas BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuración CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", 
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
            "Authorization", "Content-Disposition"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Configuración de detalles de usuario personalizada.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    /**
     * Filtro de validación JWT personalizado.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
} 