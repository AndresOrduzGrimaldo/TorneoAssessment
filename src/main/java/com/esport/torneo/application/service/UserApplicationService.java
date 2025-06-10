package com.esport.torneo.application.service;

import com.esport.torneo.application.dto.UserDto;
import com.esport.torneo.application.dto.UserRegistrationDto;
import com.esport.torneo.application.dto.UserUpdateDto;
import com.esport.torneo.application.mapper.UserMapper;
import com.esport.torneo.domain.user.User;
import com.esport.torneo.domain.user.UserRole;
import com.esport.torneo.domain.user.UserService;
import com.esport.torneo.domain.user.UserStatus;
import com.esport.torneo.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para la gestión de usuarios.
 * 
 * Orquesta las operaciones de usuarios incluyendo:
 * - Registro y autenticación
 * - Gestión de perfiles
 * - Administración de roles
 * - Verificación de email
 * - Recuperación de contraseñas
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    
    @Autowired
    public UserApplicationService(
            UserRepository userRepository,
            UserService userService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     */
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        logger.info("Registrando nuevo usuario: {}", registrationDto.getUsername());
        
        // Verificar disponibilidad de username y email
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        
        // Crear usuario usando el servicio de dominio
        User user = userService.createUser(
            registrationDto.getUsername(),
            registrationDto.getEmail(),
            registrationDto.getPassword(),
            registrationDto.getFullName()
        );
        
        // Establecer información adicional del perfil
        if (registrationDto.getPhone() != null) {
            user.setPhone(registrationDto.getPhone());
        }
        if (registrationDto.getCountry() != null) {
            user.setCountry(registrationDto.getCountry());
        }
        if (registrationDto.getCity() != null) {
            user.setCity(registrationDto.getCity());
        }
        if (registrationDto.getBirthDate() != null) {
            user.setBirthDate(registrationDto.getBirthDate());
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("Usuario registrado exitosamente con ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }
    
    /**
     * Autentica un usuario con sus credenciales.
     */
    @Transactional(readOnly = true)
    public UserDto authenticateUser(String identifier, String password) {
        logger.debug("Intentando autenticación para: {}", identifier);
        
        Optional<User> userOpt = userRepository.findByEmailOrUsername(identifier);
        if (userOpt.isEmpty()) {
            logger.warn("Usuario no encontrado: {}", identifier);
            return null;
        }
        
        User user = userOpt.get();
        
        // Verificar si el usuario está bloqueado
        if (user.isLocked()) {
            logger.warn("Intento de login de usuario bloqueado: {}", identifier);
            throw new IllegalStateException("Usuario bloqueado temporalmente");
        }
        
        // Verificar si el usuario está activo
        if (!user.getActive()) {
            logger.warn("Intento de login de usuario inactivo: {}", identifier);
            throw new IllegalStateException("Usuario inactivo");
        }
        
        // Validar credenciales
        boolean credentialsValid = userService.validateCredentials(user, password);
        
        // Registrar intento de login
        user.recordLoginAttempt(credentialsValid);
        userRepository.save(user);
        
        if (credentialsValid) {
            logger.info("Autenticación exitosa para usuario: {}", user.getUsername());
            return userMapper.toDto(user);
        } else {
            logger.warn("Credenciales inválidas para usuario: {}", identifier);
            return null;
        }
    }
    
    /**
     * Verifica el email de un usuario usando el token.
     */
    public boolean verifyEmail(String token) {
        logger.info("Verificando email con token: {}", token.substring(0, 8) + "...");
        
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token, LocalDateTime.now());
        if (userOpt.isEmpty()) {
            logger.warn("Token de verificación inválido o expirado");
            return false;
        }
        
        User user = userOpt.get();
        boolean verified = userService.verifyEmail(user, token);
        
        if (verified) {
            userRepository.save(user);
            logger.info("Email verificado exitosamente para usuario: {}", user.getUsername());
        }
        
        return verified;
    }
    
    /**
     * Reenvía el email de verificación.
     */
    public boolean resendEmailVerification(String email) {
        logger.info("Reenviando verificación de email para: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Usuario no encontrado para reenvío de verificación: {}", email);
            return false;
        }
        
        User user = userOpt.get();
        
        try {
            String token = userService.resendEmailVerification(user);
            userRepository.save(user);
            
            // Aquí se integraría con el servicio de email
            // emailService.sendVerificationEmail(user.getEmail(), token);
            
            logger.info("Email de verificación reenviado para: {}", email);
            return true;
        } catch (IllegalStateException e) {
            logger.warn("No se puede reenviar verificación: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Solicita el reseteo de contraseña.
     */
    public boolean requestPasswordReset(String email) {
        logger.info("Solicitando reseteo de contraseña para: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // No revelar si el email existe por seguridad
            logger.warn("Solicitud de reseteo para email no registrado: {}", email);
            return true; // Siempre retornar true por seguridad
        }
        
        User user = userOpt.get();
        String token = userService.generatePasswordResetToken(user);
        userRepository.save(user);
        
        // Aquí se integraría con el servicio de email
        // emailService.sendPasswordResetEmail(user.getEmail(), token);
        
        logger.info("Token de reseteo generado para: {}", email);
        return true;
    }
    
    /**
     * Resetea la contraseña usando el token.
     */
    public boolean resetPassword(String token, String newPassword) {
        logger.info("Reseteando contraseña con token: {}", token.substring(0, 8) + "...");
        
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token, LocalDateTime.now());
        if (userOpt.isEmpty()) {
            logger.warn("Token de reseteo inválido o expirado");
            return false;
        }
        
        User user = userOpt.get();
        boolean reset = userService.resetPassword(user, token, newPassword);
        
        if (reset) {
            userRepository.save(user);
            logger.info("Contraseña reseteada exitosamente para usuario: {}", user.getUsername());
        }
        
        return reset;
    }
    
    /**
     * Actualiza el perfil de un usuario.
     */
    public UserDto updateProfile(Long userId, UserUpdateDto updateDto) {
        logger.info("Actualizando perfil de usuario: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Verificar disponibilidad de email si se está cambiando
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(updateDto.getEmail(), userId)) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario");
            }
            user.setEmail(updateDto.getEmail());
            user.setEmailVerified(false); // Requerir re-verificación
        }
        
        // Actualizar información del perfil usando el servicio de dominio
        userService.updateProfile(
            user,
            updateDto.getFullName(),
            updateDto.getPhone(),
            updateDto.getCountry(),
            updateDto.getCity(),
            updateDto.getBio()
        );
        
        if (updateDto.getBirthDate() != null) {
            user.setBirthDate(updateDto.getBirthDate());
        }
        
        if (updateDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDto.getAvatarUrl());
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("Perfil actualizado para usuario: {}", userId);
        return userMapper.toDto(savedUser);
    }
    
    /**
     * Cambia la contraseña de un usuario.
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Cambiando contraseña para usuario: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Verificar contraseña actual
        if (!userService.validateCredentials(user, currentPassword)) {
            logger.warn("Contraseña actual incorrecta para usuario: {}", userId);
            return false;
        }
        
        userService.updatePassword(user, newPassword);
        userRepository.save(user);
        
        logger.info("Contraseña cambiada exitosamente para usuario: {}", userId);
        return true;
    }
    
    /**
     * Obtiene un usuario por ID.
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        return userMapper.toDto(user);
    }
    
    /**
     * Obtiene un usuario por email.
     */
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        return userMapper.toDto(user);
    }
    
    /**
     * Obtiene un usuario por username.
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        return userMapper.toDto(user);
    }
    
    /**
     * Busca usuarios con paginación.
     */
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        logger.debug("Buscando usuarios con término: {}", searchTerm);
        
        Page<User> users = searchTerm != null && !searchTerm.trim().isEmpty() ?
                userRepository.searchUsers(searchTerm, pageable) :
                userRepository.findAll(pageable);
        
        return users.map(userMapper::toDto);
    }
    
    /**
     * Obtiene usuarios por rol.
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(UserRole role, Pageable pageable) {
        logger.debug("Obteniendo usuarios con rol: {}", role);
        
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(userMapper::toDto);
    }
    
    /**
     * Obtiene usuarios por estado.
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByStatus(UserStatus status, Pageable pageable) {
        logger.debug("Obteniendo usuarios con estado: {}", status);
        
        Page<User> users = userRepository.findByStatus(status, pageable);
        return users.map(userMapper::toDto);
    }
    
    // ========== MÉTODOS ADMINISTRATIVOS ==========
    
    /**
     * Asigna un rol a un usuario (solo administradores).
     */
    public UserDto assignRole(Long userId, UserRole role, Long adminId) {
        logger.info("Asignando rol {} a usuario {} por admin {}", role, userId, adminId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + adminId));
        
        userService.assignRole(user, role, admin);
        User savedUser = userRepository.save(user);
        
        logger.info("Rol {} asignado exitosamente a usuario {}", role, userId);
        return userMapper.toDto(savedUser);
    }
    
    /**
     * Remueve un rol de un usuario (solo administradores).
     */
    public UserDto removeRole(Long userId, UserRole role, Long adminId) {
        logger.info("Removiendo rol {} de usuario {} por admin {}", role, userId, adminId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + adminId));
        
        userService.removeRole(user, role, admin);
        User savedUser = userRepository.save(user);
        
        logger.info("Rol {} removido exitosamente de usuario {}", role, userId);
        return userMapper.toDto(savedUser);
    }
    
    /**
     * Cambia el estado de un usuario (solo administradores).
     */
    public UserDto changeUserStatus(Long userId, UserStatus newStatus, Long adminId) {
        logger.info("Cambiando estado de usuario {} a {} por admin {}", userId, newStatus, adminId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + adminId));
        
        userService.changeUserStatus(user, newStatus, admin);
        User savedUser = userRepository.save(user);
        
        logger.info("Estado de usuario {} cambiado a {}", userId, newStatus);
        return userMapper.toDto(savedUser);
    }
    
    /**
     * Obtiene estadísticas de usuarios.
     */
    @Transactional(readOnly = true)
    public UserStatsDto getUserStats() {
        logger.debug("Obteniendo estadísticas de usuarios");
        
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        Object[] metrics = userRepository.getUserMetrics(last30Days);
        
        List<Object[]> statusStats = userRepository.countUsersByStatus();
        List<Object[]> roleStats = userRepository.countUsersByRole();
        
        return UserStatsDto.builder()
                .totalUsers(((Number) metrics[0]).longValue())
                .activeUsers(((Number) metrics[1]).longValue())
                .verifiedUsers(((Number) metrics[2]).longValue())
                .activeInLast30Days(((Number) metrics[3]).longValue())
                .avgDaysSinceLastLogin(metrics[4] != null ? ((Number) metrics[4]).doubleValue() : 0.0)
                .statsByStatus(statusStats)
                .statsByRole(roleStats)
                .build();
    }
    
    /**
     * Desbloquea usuarios automáticamente.
     */
    public int unlockExpiredUsers() {
        logger.info("Desbloqueando usuarios con bloqueo expirado");
        
        int unlockedCount = userRepository.unlockExpiredUsers(LocalDateTime.now());
        
        logger.info("{} usuarios desbloqueados automáticamente", unlockedCount);
        return unlockedCount;
    }
    
    /**
     * Limpia tokens expirados.
     */
    public void cleanupExpiredTokens() {
        logger.info("Limpiando tokens expirados");
        
        LocalDateTime now = LocalDateTime.now();
        int verificationTokens = userRepository.clearExpiredVerificationTokens(now);
        int passwordTokens = userRepository.clearExpiredPasswordResetTokens(now);
        
        logger.info("Tokens limpiados: {} verificación, {} reseteo", verificationTokens, passwordTokens);
    }
    
    /**
     * DTO para estadísticas de usuarios.
     */
    public static class UserStatsDto {
        private Long totalUsers;
        private Long activeUsers;
        private Long verifiedUsers;
        private Long activeInLast30Days;
        private Double avgDaysSinceLastLogin;
        private List<Object[]> statsByStatus;
        private List<Object[]> statsByRole;
        
        // Constructor, getters, setters y builder
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private UserStatsDto stats = new UserStatsDto();
            
            public Builder totalUsers(Long totalUsers) {
                stats.totalUsers = totalUsers;
                return this;
            }
            
            public Builder activeUsers(Long activeUsers) {
                stats.activeUsers = activeUsers;
                return this;
            }
            
            public Builder verifiedUsers(Long verifiedUsers) {
                stats.verifiedUsers = verifiedUsers;
                return this;
            }
            
            public Builder activeInLast30Days(Long activeInLast30Days) {
                stats.activeInLast30Days = activeInLast30Days;
                return this;
            }
            
            public Builder avgDaysSinceLastLogin(Double avgDaysSinceLastLogin) {
                stats.avgDaysSinceLastLogin = avgDaysSinceLastLogin;
                return this;
            }
            
            public Builder statsByStatus(List<Object[]> statsByStatus) {
                stats.statsByStatus = statsByStatus;
                return this;
            }
            
            public Builder statsByRole(List<Object[]> statsByRole) {
                stats.statsByRole = statsByRole;
                return this;
            }
            
            public UserStatsDto build() {
                return stats;
            }
        }
        
        // Getters
        public Long getTotalUsers() { return totalUsers; }
        public Long getActiveUsers() { return activeUsers; }
        public Long getVerifiedUsers() { return verifiedUsers; }
        public Long getActiveInLast30Days() { return activeInLast30Days; }
        public Double getAvgDaysSinceLastLogin() { return avgDaysSinceLastLogin; }
        public List<Object[]> getStatsByStatus() { return statsByStatus; }
        public List<Object[]> getStatsByRole() { return statsByRole; }
    }
} 