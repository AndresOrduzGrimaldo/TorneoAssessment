package com.esport.torneo.domain.notification;

/**
 * Enumeración que define la prioridad de las notificaciones.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum NotificationPriority {
    /**
     * Prioridad baja - notificaciones informativas.
     */
    LOW("low", "Baja", 1),
    
    /**
     * Prioridad normal - notificaciones estándar.
     */
    NORMAL("normal", "Normal", 2),
    
    /**
     * Prioridad alta - notificaciones urgentes.
     */
    HIGH("high", "Alta", 3),
    
    /**
     * Prioridad crítica - notificaciones que requieren acción inmediata.
     */
    CRITICAL("critical", "Crítica", 4);
    
    private final String code;
    private final String displayName;
    private final int level;
    
    NotificationPriority(String code, String displayName, int level) {
        this.code = code;
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Verifica si esta prioridad es mayor que otra.
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Busca una prioridad por su código.
     */
    public static NotificationPriority fromCode(String code) {
        for (NotificationPriority priority : values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Prioridad de notificación no válida: " + code);
    }
} 