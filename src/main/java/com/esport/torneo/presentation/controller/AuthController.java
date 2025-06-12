package com.esport.torneo.presentation.controller;

import com.esport.torneo.application.dto.UserDto;
import com.esport.torneo.application.dto.UserRegistrationDto;
import com.esport.torneo.application.service.UserApplicationService;
import com.esport.torneo.config.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Controlador para autenticación y autorización.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API para autenticación y autorización")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserApplicationService userService;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna tokens JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        
        logger.info("Intento de login para usuario: {}", loginRequest.getUsername());
        
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Generar tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

            // Obtener información del usuario
            UserDto userDto = userService.getUserByUsername(authentication.getName());

            LoginResponse response = new LoginResponse(
                "Login exitoso",
                accessToken,
                refreshToken,
                "Bearer",
                86400L, // 24 horas en segundos
                userDto
            );

            logger.info("Login exitoso para usuario: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            logger.warn("Fallo en autenticación para usuario: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Credenciales inválidas", null, null, null, null, null));
        }
    }

    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
        @ApiResponse(responseCode = "409", description = "Usuario ya existe"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        
        logger.info("Intento de registro para usuario: {}", registrationDto.getUsername());
        
        try {
            UserDto createdUser = userService.createUser(registrationDto);
            
            RegisterResponse response = new RegisterResponse(
                "Usuario registrado exitosamente",
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getEmail(),
                LocalDateTime.now()
            );

            logger.info("Usuario registrado exitosamente: {}", registrationDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RegisterResponse(e.getMessage(), null, null, null, null));
        }
    }

    @Operation(summary = "Refrescar token", description = "Obtiene un nuevo access token usando el refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        
        logger.debug("Solicitud de refresh token");
        
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            
            if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new BadCredentialsException("Refresh token inválido");
            }

            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            UserDto userDto = userService.getUserByUsername(username);
            
            // Generar nuevo access token
            String newAccessToken = jwtTokenProvider.generateTokenForUser(
                username, 
                Collections.singletonList("ROLE_" + userDto.getRole().name())
            );

            RefreshTokenResponse response = new RefreshTokenResponse(
                "Token refrescado exitosamente",
                newAccessToken,
                "Bearer",
                86400L
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Error al refrescar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new RefreshTokenResponse("Refresh token inválido", null, null, null));
        }
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el token de acceso (logout)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout exitoso")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        // En un sistema real, aquí se invalidaría el token en una blacklist
        // Por simplicidad, solo retornamos un mensaje de éxito
        logger.info("Usuario cerró sesión");
        
        LogoutResponse response = new LogoutResponse(
            "Sesión cerrada exitosamente",
            LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }

    // DTOs para requests y responses

    @Schema(description = "Datos para iniciar sesión")
    public static class LoginRequest {
        @Schema(description = "Nombre de usuario o email", example = "johndoe")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        private String username;

        @Schema(description = "Contraseña", example = "password123")
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @Schema(description = "Respuesta de login exitoso")
    public static class LoginResponse {
        private String message;
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserDto user;

        public LoginResponse(String message, String accessToken, String refreshToken, 
                           String tokenType, Long expiresIn, UserDto user) {
            this.message = message;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.user = user;
        }

        // Getters y setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        public UserDto getUser() { return user; }
        public void setUser(UserDto user) { this.user = user; }
    }

    @Schema(description = "Respuesta de registro exitoso")
    public static class RegisterResponse {
        private String message;
        private Long userId;
        private String username;
        private String email;
        private LocalDateTime timestamp;

        public RegisterResponse(String message, Long userId, String username, String email, LocalDateTime timestamp) {
            this.message = message;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.timestamp = timestamp;
        }

        // Getters y setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    @Schema(description = "Request para refrescar token")
    public static class RefreshTokenRequest {
        @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    @Schema(description = "Respuesta de refresh token")
    public static class RefreshTokenResponse {
        private String message;
        private String accessToken;
        private String tokenType;
        private Long expiresIn;

        public RefreshTokenResponse(String message, String accessToken, String tokenType, Long expiresIn) {
            this.message = message;
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
        }

        // Getters y setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    }

    @Schema(description = "Respuesta de logout")
    public static class LogoutResponse {
        private String message;
        private LocalDateTime timestamp;

        public LogoutResponse(String message, LocalDateTime timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters y setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
} 