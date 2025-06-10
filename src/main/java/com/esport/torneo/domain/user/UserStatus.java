package com.esport.torneo.domain.user;

/**
 * Enumeración que define los estados de usuario en el sistema.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum UserStatus {
    /**
     * Usuario activo - puede usar todas las funcionalidades.
     */
    ACTIVE("active", "Activo"),
    
    /**
     * Usuario inactivo - cuenta deshabilitada temporalmente.
     */
    INACTIVE("inactive", "Inactivo"),
    
    /**
     * Usuario suspendido - sancionado por violación de términos.
     */
    SUSPENDED("suspended", "Suspendido"),
    
    /**
     * Usuario bloqueado - bloqueado temporalmente por intentos fallidos.
     */
    LOCKED("locked", "Bloqueado"),
    
    /**
     * Usuario pendiente de verificación.
     */
    PENDING_VERIFICATION("pending_verification", "Pendiente de Verificación");
    
    private final String code;
    private final String displayName;
    
    UserStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica si el usuario puede iniciar sesión.
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    /**
     * Verifica si el estado permite transición a otro estado.
     */
    public boolean canTransitionTo(UserStatus newStatus) {
        if (this == newStatus) {
            return false;
        }
        
        switch (this) {
            case PENDING_VERIFICATION:
                return newStatus == ACTIVE || newStatus == INACTIVE;
            case ACTIVE:
                return newStatus == INACTIVE || newStatus == SUSPENDED || newStatus == LOCKED;
            case INACTIVE:
                return newStatus == ACTIVE || newStatus == SUSPENDED;
            case SUSPENDED:
                return newStatus == ACTIVE || newStatus == INACTIVE;
            case LOCKED:
                return newStatus == ACTIVE || newStatus == INACTIVE;
            default:
                return false;
        }
    }
    
    /**
     * Verifica si el estado es temporal.
     */
    public boolean isTemporary() {
        return this == LOCKED || this == PENDING_VERIFICATION;
    }
    
    /**
     * Busca un estado por su código.
     */
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de usuario no válido: " + code);
    }
} 