package com.esport.torneo.domain.notification;

/**
 * Enumeración que define los canales de envío de notificaciones.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum NotificationChannel {
    /**
     * Notificación por correo electrónico.
     */
    EMAIL("email", "Correo Electrónico"),
    
    /**
     * Notificación en tiempo real via WebSocket.
     */
    WEBSOCKET("websocket", "Tiempo Real"),
    
    /**
     * Notificación push para móviles.
     */
    PUSH("push", "Push Móvil"),
    
    /**
     * Notificación SMS.
     */
    SMS("sms", "SMS");
    
    private final String code;
    private final String displayName;
    
    NotificationChannel(String code, String displayName) {
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
     * Busca un canal por su código.
     */
    public static NotificationChannel fromCode(String code) {
        for (NotificationChannel channel : values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Canal de notificación no válido: " + code);
    }
} 