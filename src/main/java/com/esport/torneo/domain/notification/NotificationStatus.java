package com.esport.torneo.domain.notification;

/**
 * Enumeración que define los estados posibles de una notificación.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum NotificationStatus {
    /**
     * Notificación pendiente de envío.
     */
    PENDING("Pendiente"),
    
    /**
     * Notificación enviada exitosamente.
     */
    SENT("Enviada"),
    
    /**
     * Error en el envío de la notificación.
     */
    FAILED("Fallida"),
    
    /**
     * Notificación leída por el usuario.
     */
    READ("Leída");
    
    private final String displayName;
    
    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica si se puede transicionar a un nuevo estado.
     * 
     * @param newStatus el nuevo estado
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(NotificationStatus newStatus) {
        if (this == newStatus) {
            return false;
        }
        
        switch (this) {
            case PENDING:
                return newStatus == SENT || newStatus == FAILED;
            case SENT:
                return newStatus == READ;
            case FAILED:
                return newStatus == SENT || newStatus == PENDING; // Permitir reintentos
            case READ:
                return false; // Estado final
            default:
                return false;
        }
    }
    
    /**
     * Verifica si el estado es final (no permite más transiciones).
     */
    public boolean isFinal() {
        return this == READ;
    }
    
    /**
     * Verifica si el estado permite reintentos.
     */
    public boolean allowsRetry() {
        return this == FAILED;
    }
} 