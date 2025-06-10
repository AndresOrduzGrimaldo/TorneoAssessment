package com.esport.torneo.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esport.torneo.domain.user.User;
import com.esport.torneo.domain.user.UserRole;
import com.esport.torneo.domain.user.UserStatus;

/**
 * Repositorio para la gestión de usuarios.
 * 
 * Proporciona operaciones de persistencia y consultas específicas para:
 * - Autenticación y verificación
 * - Búsqueda por roles y estados
 * - Gestión de tokens
 * - Métricas y estadísticas
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por email.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Busca un usuario por nombre de usuario.
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.active = true")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * Busca un usuario por email o username para login.
     */
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.username = :identifier) AND u.active = true")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);
    
    /**
     * Verifica si existe un usuario con el email dado.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.active = true")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Verifica si existe un usuario con el username dado.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.active = true")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Verifica si existe un usuario con el email dado, excluyendo un ID específico.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :excludeId AND u.active = true")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);
    
    /**
     * Verifica si existe un usuario con el username dado, excluyendo un ID específico.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :excludeId AND u.active = true")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("excludeId") Long excludeId);
    
    /**
     * Busca un usuario por token de verificación de email.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationExpiresAt > :now AND u.active = true")
    Optional<User> findByEmailVerificationToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Busca un usuario por token de reseteo de contraseña.
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpiresAt > :now AND u.active = true")
    Optional<User> findByPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Busca usuarios por estado.
     */
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> findByStatus(@Param("status") UserStatus status, Pageable pageable);
    
    /**
     * Busca usuarios por rol.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> findByRole(@Param("role") UserRole role, Pageable pageable);
    
    /**
     * Busca usuarios por múltiples roles.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN :roles AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> findByRolesIn(@Param("roles") List<UserRole> roles, Pageable pageable);
    
    /**
     * Busca usuarios activos.
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.active = true ORDER BY u.lastLoginAt DESC NULLS LAST")
    Page<User> findActiveUsers(Pageable pageable);
    
    /**
     * Busca usuarios con email no verificado.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.active = true ORDER BY u.createdAt ASC")
    Page<User> findUnverifiedUsers(Pageable pageable);
    
    /**
     * Busca usuarios bloqueados.
     */
    @Query("SELECT u FROM User u WHERE u.status = 'LOCKED' AND u.lockedUntil IS NOT NULL AND u.lockedUntil > :now AND u.active = true")
    List<User> findCurrentlyLockedUsers(@Param("now") LocalDateTime now);
    
    /**
     * Busca usuarios para desbloquear automáticamente.
     */
    @Query("SELECT u FROM User u WHERE u.status = 'LOCKED' AND u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now AND u.active = true")
    List<User> findUsersToUnlock(@Param("now") LocalDateTime now);
    
    /**
     * Busca usuarios con tokens de verificación expirados.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken IS NOT NULL AND u.emailVerificationExpiresAt <= :now AND u.emailVerified = false AND u.active = true")
    List<User> findUsersWithExpiredVerificationTokens(@Param("now") LocalDateTime now);
    
    /**
     * Busca usuarios por país.
     */
    @Query("SELECT u FROM User u WHERE u.country = :country AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> findByCountry(@Param("country") String country, Pageable pageable);
    
    /**
     * Busca usuarios con último login dentro de un rango de fechas.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :start AND :end AND u.active = true ORDER BY u.lastLoginAt DESC")
    Page<User> findByLastLoginBetween(@Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end, 
                                    Pageable pageable);
    
    /**
     * Busca usuarios registrados en un rango de fechas.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> findByCreatedAtBetween(@Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end, 
                                    Pageable pageable);
    
    /**
     * Búsqueda de usuarios por texto (nombre, email, username).
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.active = true ORDER BY u.createdAt DESC")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Cuenta usuarios por estado.
     */
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.status")
    List<Object[]> countUsersByStatus();
    
    /**
     * Cuenta usuarios por rol.
     */
    @Query("SELECT r, COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE u.active = true GROUP BY r")
    List<Object[]> countUsersByRole();
    
    /**
     * Obtiene estadísticas de usuarios registrados por mes.
     */
    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) FROM User u WHERE u.createdAt >= :since AND u.active = true GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("since") LocalDateTime since);
    
    /**
     * Obtiene estadísticas de último login.
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.lastLoginAt >= :last24h THEN 1 END) as activeToday, " +
           "COUNT(CASE WHEN u.lastLoginAt >= :last7d THEN 1 END) as activeWeek, " +
           "COUNT(CASE WHEN u.lastLoginAt >= :last30d THEN 1 END) as activeMonth, " +
           "COUNT(u) as total " +
           "FROM User u WHERE u.active = true")
    Object[] getUserActivityStats(@Param("last24h") LocalDateTime last24h,
                                @Param("last7d") LocalDateTime last7d,
                                @Param("last30d") LocalDateTime last30d);
    
    /**
     * Obtiene usuarios más activos por último login.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL AND u.active = true ORDER BY u.lastLoginAt DESC")
    Page<User> findMostActiveUsers(Pageable pageable);
    
    /**
     * Busca organizadores activos.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'ORGANIZER' AND u.status = 'ACTIVE' AND u.active = true ORDER BY u.createdAt DESC")
    List<User> findActiveOrganizers();
    
    /**
     * Busca administradores activos.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'ADMIN' AND u.status = 'ACTIVE' AND u.active = true ORDER BY u.createdAt DESC")
    List<User> findActiveAdmins();
    
    /**
     * Actualiza el último login de un usuario.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * Limpia tokens de verificación expirados.
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerificationToken = null, u.emailVerificationExpiresAt = null WHERE u.emailVerificationExpiresAt <= :now")
    int clearExpiredVerificationTokens(@Param("now") LocalDateTime now);
    
    /**
     * Limpia tokens de reseteo de contraseña expirados.
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpiresAt = null WHERE u.passwordResetExpiresAt <= :now")
    int clearExpiredPasswordResetTokens(@Param("now") LocalDateTime now);
    
    /**
     * Desbloquea usuarios automáticamente.
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'ACTIVE', u.lockedUntil = null, u.failedLoginAttempts = 0 WHERE u.status = 'LOCKED' AND u.lockedUntil <= :now")
    int unlockExpiredUsers(@Param("now") LocalDateTime now);
    
    /**
     * Elimina usuarios antiguos (soft delete).
     */
    @Modifying
    @Query("UPDATE User u SET u.active = false, u.deletedAt = :now WHERE u.createdAt < :before AND u.lastLoginAt IS NULL")
    int softDeleteInactiveUsers(@Param("before") LocalDateTime before, @Param("now") LocalDateTime now);
    
    /**
     * Busca usuarios duplicados por email.
     */
    @Query("SELECT u.email, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.email HAVING COUNT(u) > 1")
    List<Object[]> findDuplicateEmails();
    
    /**
     * Busca usuarios duplicados por username.
     */
    @Query("SELECT u.username, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.username HAVING COUNT(u) > 1")
    List<Object[]> findDuplicateUsernames();
    
    /**
     * Obtiene usuarios con múltiples intentos fallidos recientes.
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold AND u.active = true ORDER BY u.failedLoginAttempts DESC")
    List<User> findUsersWithMultipleFailedAttempts(@Param("threshold") int threshold);
    
    /**
     * Busca usuarios por edad (calculada).
     */
    @Query("SELECT u FROM User u WHERE u.birthDate IS NOT NULL AND " +
           "YEAR(CURRENT_DATE) - YEAR(u.birthDate) BETWEEN :minAge AND :maxAge AND u.active = true")
    Page<User> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge, Pageable pageable);
    
    /**
     * Obtiene métricas completas de usuarios.
     */
    @Query("""
        SELECT 
            COUNT(u) as totalUsers,
            COUNT(CASE WHEN u.status = 'ACTIVE' THEN 1 END) as activeUsers,
            COUNT(CASE WHEN u.emailVerified = true THEN 1 END) as verifiedUsers,
            COUNT(CASE WHEN u.lastLoginAt >= :last30d THEN 1 END) as activeInLast30Days,
            AVG(CASE WHEN u.lastLoginAt IS NOT NULL THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - u.lastLoginAt))/86400 END) as avgDaysSinceLastLogin
        FROM User u 
        WHERE u.active = true
        """)
    Object[] getUserMetrics(@Param("last30d") LocalDateTime last30d);
} 