package com.esport.torneo.domain.user;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio para la gestión de usuarios.
 * 
 * Contiene la lógica de negocio para:
 * - Creación y validación de usuarios
 * - Gestión de contraseñas
 * - Verificación de email
 * - Gestión de roles y permisos
 * - Validaciones de negocio
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Service
public class UserService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,50}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Crea un nuevo usuario con validaciones de negocio.
     */
    public User createUser(String username, String email, String password, String fullName) {
        // Validaciones
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);
        validateFullName(fullName);
        
        // Encriptar contraseña
        String encodedPassword = passwordEncoder.encode(password);
        
        // Crear usuario
        User user = new User(username, email, encodedPassword, fullName);
        
        // Generar token de verificación de email
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken, 24); // 24 horas
        
        return user;
    }
    
    /**
     * Valida las credenciales de un usuario.
     */
    public boolean validateCredentials(User user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }
        
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
    
    /**
     * Actualiza la contraseña de un usuario.
     */
    public void updatePassword(User user, String newPassword) {
        validatePassword(newPassword);
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
    }
    
    /**
     * Genera un token de reseteo de contraseña.
     */
    public String generatePasswordResetToken(User user) {
        String token = generateSecureToken();
        user.setPasswordResetToken(token, 2); // 2 horas
        return token;
    }
    
    /**
     * Resetea la contraseña usando un token.
     */
    public boolean resetPassword(User user, String token, String newPassword) {
        if (!user.isPasswordResetTokenValid(token)) {
            return false;
        }
        
        updatePassword(user, newPassword);
        return true;
    }
    
    /**
     * Verifica el email de un usuario usando un token.
     */
    public boolean verifyEmail(User user, String token) {
        if (!user.isEmailVerificationTokenValid(token)) {
            return false;
        }
        
        user.verifyEmail();
        return true;
    }
    
    /**
     * Reenvía el token de verificación de email.
     */
    public String resendEmailVerification(User user) {
        if (user.getEmailVerified()) {
            throw new IllegalStateException("El email ya está verificado");
        }
        
        String token = generateSecureToken();
        user.setEmailVerificationToken(token, 24); // 24 horas
        return token;
    }
    
    /**
     * Asigna un rol a un usuario.
     */
    public void assignRole(User user, UserRole role, User assignedBy) {
        // Validar permisos
        if (!canAssignRole(assignedBy, role)) {
            throw new IllegalArgumentException("No tienes permisos para asignar este rol");
        }
        
        user.addRole(role);
        
        // Si se asigna rol de organizador, verificar límites
        if (role == UserRole.ORGANIZER) {
            validateOrganizerAssignment(user);
        }
    }
    
    /**
     * Remueve un rol de un usuario.
     */
    public void removeRole(User user, UserRole role, User removedBy) {
        // Validar permisos
        if (!canRemoveRole(removedBy, role)) {
            throw new IllegalArgumentException("No tienes permisos para remover este rol");
        }
        
        // No permitir remover el último rol USER
        if (role == UserRole.USER && user.getRoles().size() == 1) {
            throw new IllegalArgumentException("No se puede remover el último rol de usuario");
        }
        
        user.removeRole(role);
    }
    
    /**
     * Actualiza el perfil de un usuario.
     */
    public void updateProfile(User user, String fullName, String phone, String country, String city, String bio) {
        if (fullName != null) {
            validateFullName(fullName);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            validatePhone(phone);
        }
        
        if (bio != null && bio.length() > 500) {
            throw new IllegalArgumentException("La biografía no puede exceder 500 caracteres");
        }
        
        user.updateProfile(fullName, phone, country, city, bio);
    }
    
    /**
     * Cambia el estado de un usuario.
     */
    public void changeUserStatus(User user, UserStatus newStatus, User changedBy) {
        // Validar permisos
        if (!canChangeUserStatus(changedBy, user, newStatus)) {
            throw new IllegalArgumentException("No tienes permisos para cambiar el estado de este usuario");
        }
        
        // Validar transición de estado
        if (!user.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalArgumentException("Transición de estado no válida");
        }
        
        switch (newStatus) {
            case ACTIVE:
                user.activate();
                break;
            case INACTIVE:
                user.deactivate();
                break;
            case SUSPENDED:
                user.suspend();
                break;
            case LOCKED:
                user.lockTemporarily(30); // 30 minutos por defecto
                break;
        }
    }
    
    /**
     * Valida si un usuario puede asignar un rol.
     */
    private boolean canAssignRole(User assignedBy, UserRole role) {
        if (assignedBy == null || !assignedBy.isActive()) {
            return false;
        }
        
        // Solo ADMIN puede asignar roles
        return assignedBy.hasRole(UserRole.ADMIN);
    }
    
    /**
     * Valida si un usuario puede remover un rol.
     */
    private boolean canRemoveRole(User removedBy, UserRole role) {
        if (removedBy == null || !removedBy.isActive()) {
            return false;
        }
        
        // Solo ADMIN puede remover roles
        return removedBy.hasRole(UserRole.ADMIN);
    }
    
    /**
     * Valida si un usuario puede cambiar el estado de otro usuario.
     */
    private boolean canChangeUserStatus(User changedBy, User targetUser, UserStatus newStatus) {
        if (changedBy == null || !changedBy.isActive()) {
            return false;
        }
        
        // No puede cambiar su propio estado
        if (changedBy.getId().equals(targetUser.getId())) {
            return false;
        }
        
        // Solo ADMIN puede cambiar estados
        return changedBy.hasRole(UserRole.ADMIN);
    }
    
    /**
     * Valida la asignación de rol organizador.
     */
    private void validateOrganizerAssignment(User user) {
        // Aquí se pueden agregar validaciones específicas para organizadores
        // Por ejemplo, verificar que el usuario tenga email verificado
        if (!user.getEmailVerified()) {
            throw new IllegalArgumentException("El usuario debe tener el email verificado para ser organizador");
        }
    }
    
    /**
     * Valida el nombre de usuario.
     */
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos");
        }
    }
    
    /**
     * Valida el email.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email debe tener un formato válido");
        }
    }
    
    /**
     * Valida la contraseña.
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y símbolos");
        }
    }
    
    /**
     * Valida el nombre completo.
     */
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new IllegalArgumentException("El nombre completo debe tener entre 2 y 100 caracteres");
        }
    }
    
    /**
     * Valida el teléfono.
     */
    private void validatePhone(String phone) {
        if (phone.length() > 20) {
            throw new IllegalArgumentException("El teléfono no puede exceder 20 caracteres");
        }
        
        // Validación básica de formato de teléfono
        if (!phone.matches("^[+]?[0-9\\s\\-\\(\\)]{7,20}$")) {
            throw new IllegalArgumentException("El formato del teléfono no es válido");
        }
    }
    
    /**
     * Genera un token seguro.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Verifica si un username está disponible.
     */
    public boolean isUsernameAvailable(String username) {
        validateUsername(username);
        // Esta validación se completará en el repositorio
        return true;
    }
    
    /**
     * Verifica si un email está disponible.
     */
    public boolean isEmailAvailable(String email) {
        validateEmail(email);
        // Esta validación se completará en el repositorio
        return true;
    }
    
    /**
     * Calcula la fuerza de una contraseña.
     */
    public PasswordStrength calculatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // Longitud
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Complejidad
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@$!%*?&].*")) score++;
        
        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }
    
    /**
     * Enumeración para la fuerza de contraseña.
     */
    public enum PasswordStrength {
        WEAK("Débil"),
        MEDIUM("Media"),
        STRONG("Fuerte");
        
        private final String displayName;
        
        PasswordStrength(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 