package com.esport.torneo.domain.user;

/**
 * Enumeración que define los roles de usuario en el sistema.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum UserRole {
    /**
     * Usuario básico - puede participar en torneos y comprar tickets.
     */
    USER("user", "Usuario", "Puede participar en torneos y comprar tickets"),
    
    /**
     * Organizador - puede crear y gestionar torneos.
     */
    ORGANIZER("organizer", "Organizador", "Puede crear y gestionar torneos"),
    
    /**
     * Administrador - acceso completo al sistema.
     */
    ADMIN("admin", "Administrador", "Acceso completo al sistema");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    UserRole(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica si este rol tiene mayor autoridad que otro.
     */
    public boolean hasHigherAuthorityThan(UserRole other) {
        return this.ordinal() > other.ordinal();
    }
    
    /**
     * Verifica si este rol puede gestionar otro rol.
     */
    public boolean canManage(UserRole other) {
        // Solo ADMIN puede gestionar otros roles
        return this == ADMIN && other != ADMIN;
    }
    
    /**
     * Busca un rol por su código.
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol de usuario no válido: " + code);
    }
    
    /**
     * Verifica si el rol es administrativo.
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == ORGANIZER;
    }
} 