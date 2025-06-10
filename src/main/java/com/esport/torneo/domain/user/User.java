package com.esport.torneo.domain.user;

import com.esport.torneo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un usuario en el sistema.
 * 
 * Un usuario puede tener diferentes roles:
 * - USER: Usuario básico que puede participar en torneos
 * - ORGANIZER: Puede crear y gestionar torneos
 * - ADMIN: Administrador del sistema
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
public class User extends BaseEntity {
    
    /**
     * Nombre de usuario único.
     */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * Correo electrónico único.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * Contraseña encriptada.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 60, max = 60, message = "La contraseña encriptada debe tener 60 caracteres")
    @Column(name = "password", nullable = false, length = 60)
    private String password;
    
    /**
     * Nombre completo del usuario.
     */
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    /**
     * Estado del usuario.
     */
    @NotNull(message = "El estado del usuario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;
    
    /**
     * Roles del usuario.
     */
    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 20)
    private Set<UserRole> roles = new HashSet<>();
    
    /**
     * Avatar del usuario (URL o path).
     */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    /**
     * Teléfono del usuario.
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * Fecha de nacimiento.
     */
    @Column(name = "birth_date")
    private LocalDateTime birthDate;
    
    /**
     * País del usuario.
     */
    @Size(max = 50, message = "El país no puede exceder 50 caracteres")
    @Column(name = "country", length = 50)
    private String country;
    
    /**
     * Ciudad del usuario.
     */
    @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
    @Column(name = "city", length = 50)
    private String city;
    
    /**
     * Biografía o descripción del usuario.
     */
    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    @Column(name = "bio", length = 500)
    private String bio;
    
    /**
     * Fecha de último acceso.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * Indica si el email está verificado.
     */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    /**
     * Token de verificación de email.
     */
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;
    
    /**
     * Fecha de expiración del token de verificación.
     */
    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;
    
    /**
     * Token de reseteo de contraseña.
     */
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;
    
    /**
     * Fecha de expiración del token de reseteo.
     */
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    /**
     * Número de intentos fallidos de login.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;
    
    /**
     * Fecha hasta la cual el usuario está bloqueado.
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    /**
     * Configuraciones de notificaciones en formato JSON.
     */
    @Column(name = "notification_preferences", columnDefinition = "jsonb")
    private String notificationPreferences;
    
    /**
     * Constructor por defecto para JPA.
     */
    protected User() {
        super();
    }
    
    /**
     * Constructor para crear un nuevo usuario.
     */
    public User(String username, String email, String password, String fullName) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.failedLoginAttempts = 0;
        this.roles.add(UserRole.USER); // Rol por defecto
    }
    
    /**
     * Agrega un rol al usuario.
     */
    public void addRole(UserRole role) {
        this.roles.add(role);
    }
    
    /**
     * Remueve un rol del usuario.
     */
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }
    
    /**
     * Verifica si el usuario tiene un rol específico.
     */
    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }
    
    /**
     * Verifica si el usuario es administrador.
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
    
    /**
     * Verifica si el usuario es organizador.
     */
    public boolean isOrganizer() {
        return hasRole(UserRole.ORGANIZER);
    }
    
    /**
     * Activa el usuario.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }
    
    /**
     * Desactiva el usuario.
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    /**
     * Suspende el usuario.
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }
    
    /**
     * Bloquea el usuario temporalmente.
     */
    public void lockTemporarily(int minutes) {
        this.status = UserStatus.LOCKED;
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }
    
    /**
     * Verifica si el usuario está activo.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked();
    }
    
    /**
     * Verifica si el usuario está bloqueado.
     */
    public boolean isLocked() {
        return status == UserStatus.LOCKED && 
               lockedUntil != null && 
               lockedUntil.isAfter(LocalDateTime.now());
    }
    
    /**
     * Verifica la contraseña del usuario.
     */
    public void recordLoginAttempt(boolean successful) {
        if (successful) {
            this.failedLoginAttempts = 0;
            this.lastLoginAt = LocalDateTime.now();
            // Desbloquear si estaba bloqueado temporalmente
            if (isLocked() && lockedUntil.isBefore(LocalDateTime.now())) {
                this.status = UserStatus.ACTIVE;
                this.lockedUntil = null;
            }
        } else {
            this.failedLoginAttempts++;
            // Bloquear después de 5 intentos fallidos
            if (this.failedLoginAttempts >= 5) {
                lockTemporarily(30); // 30 minutos
            }
        }
    }
    
    /**
     * Verifica el email del usuario.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }
    
    /**
     * Establece un token de verificación de email.
     */
    public void setEmailVerificationToken(String token, int expirationHours) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiresAt = LocalDateTime.now().plusHours(expirationHours);
    }
    
    /**
     * Establece un token de reseteo de contraseña.
     */
    public void setPasswordResetToken(String token, int expirationHours) {
        this.passwordResetToken = token;
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(expirationHours);
    }
    
    /**
     * Limpia el token de reseteo de contraseña.
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
    }
    
    /**
     * Verifica si el token de verificación de email es válido.
     */
    public boolean isEmailVerificationTokenValid(String token) {
        return emailVerificationToken != null &&
               emailVerificationToken.equals(token) &&
               emailVerificationExpiresAt != null &&
               emailVerificationExpiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * Verifica si el token de reseteo de contraseña es válido.
     */
    public boolean isPasswordResetTokenValid(String token) {
        return passwordResetToken != null &&
               passwordResetToken.equals(token) &&
               passwordResetExpiresAt != null &&
               passwordResetExpiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * Actualiza la contraseña del usuario.
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        clearPasswordResetToken();
    }
    
    /**
     * Actualiza el perfil del usuario.
     */
    public void updateProfile(String fullName, String phone, String country, String city, String bio) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
        }
        this.phone = phone;
        this.country = country;
        this.city = city;
        this.bio = bio;
    }
    
    /**
     * Calcula la edad del usuario.
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDateTime.now().getYear() - birthDate.getYear();
    }
    
    // Getters y Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }
    
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }
    
    public LocalDateTime getEmailVerificationExpiresAt() {
        return emailVerificationExpiresAt;
    }
    
    public void setEmailVerificationExpiresAt(LocalDateTime emailVerificationExpiresAt) {
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
    }
    
    public String getPasswordResetToken() {
        return passwordResetToken;
    }
    
    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
    
    public LocalDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }
    
    public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }
    
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public String getNotificationPreferences() {
        return notificationPreferences;
    }
    
    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status=" + status +
                ", roles=" + roles +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
} 