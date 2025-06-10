package com.esport.torneo.presentation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esport.torneo.application.dto.UserDto;
import com.esport.torneo.application.dto.UserRegistrationDto;
import com.esport.torneo.application.dto.UserUpdateDto;
import com.esport.torneo.application.service.UserApplicationService;
import com.esport.torneo.domain.user.UserRole;
import com.esport.torneo.domain.user.UserStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Controlador REST para la gestión de usuarios.
 * 
 * Proporciona endpoints para:
 * - Registro y autenticación de usuarios
 * - Gestión de perfiles
 * - Búsqueda y listado de usuarios
 * - Administración de usuarios (roles, estados)
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserApplicationService userApplicationService;
    
    @Autowired
    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
        @ApiResponse(responseCode = "409", description = "Usuario ya existe")
    })
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        logger.info("Solicitud de registro para usuario: {}", registrationDto.getUsername());
        
        try {
            UserDto newUser = userApplicationService.registerUser(registrationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Autentica un usuario.
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<UserDto> loginUser(@RequestBody LoginRequest loginRequest) {
        logger.debug("Solicitud de login para: {}", loginRequest.getIdentifier());
        
        try {
            UserDto user = userApplicationService.authenticateUser(
                loginRequest.getIdentifier(), 
                loginRequest.getPassword()
            );
            
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
    }
    
    /**
     * Obtiene usuarios con paginación.
     */
    @GetMapping
    @Operation(summary = "Buscar usuarios")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<UserDto> users = role != null ? 
            userApplicationService.getUsersByRole(role, pageable) :
            userApplicationService.searchUsers(search, pageable);
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Obtiene un usuario por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        try {
            UserDto user = userApplicationService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Actualiza un usuario.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto updateDto) {
        
        try {
            UserDto updatedUser = userApplicationService.updateProfile(id, updateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Asigna rol a usuario (admin).
     */
    @PostMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar rol a usuario")
    public ResponseEntity<UserDto> assignRole(
            @PathVariable Long userId,
            @PathVariable UserRole role,
            @RequestAttribute("userId") Long adminId) {
        
        try {
            UserDto updatedUser = userApplicationService.assignRole(userId, role, adminId);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cambia estado de usuario (admin).
     */
    @PutMapping("/{userId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado de usuario")
    public ResponseEntity<UserDto> changeUserStatus(
            @PathVariable Long userId,
            @PathVariable UserStatus status,
            @RequestAttribute("userId") Long adminId) {
        
        try {
            UserDto updatedUser = userApplicationService.changeUserStatus(userId, status, adminId);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ========== DTOs DE APOYO ==========
    
    public static class LoginRequest {
        @NotBlank
        private String identifier;
        
        @NotBlank
        private String password;
        
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
} 