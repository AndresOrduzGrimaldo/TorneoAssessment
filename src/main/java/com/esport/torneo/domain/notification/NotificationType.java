package com.esport.torneo.domain.notification;

/**
 * Enumeración que define los tipos de notificaciones disponibles en el sistema.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public enum NotificationType {
    /**
     * Notificación de registro en torneo.
     */
    TOURNAMENT_REGISTRATION("Registro en Torneo"),
    
    /**
     * Notificación de inicio de torneo.
     */
    TOURNAMENT_START("Inicio de Torneo"),
    
    /**
     * Notificación de finalización de torneo.
     */
    TOURNAMENT_END("Finalización de Torneo"),
    
    /**
     * Notificación de cancelación de torneo.
     */
    TOURNAMENT_CANCELLED("Cancelación de Torneo"),
    
    /**
     * Notificación de confirmación de ticket.
     */
    TICKET_CONFIRMATION("Confirmación de Ticket"),
    
    /**
     * Notificación de pago de ticket.
     */
    TICKET_PAYMENT("Pago de Ticket"),
    
    /**
     * Notificación de expiración de ticket.
     */
    TICKET_EXPIRED("Expiración de Ticket"),
    
    /**
     * Notificación de resultado de partida.
     */
    MATCH_RESULT("Resultado de Partida"),
    
    /**
     * Notificación de próxima partida.
     */
    MATCH_UPCOMING("Próxima Partida"),
    
    /**
     * Notificación de recordatorio general.
     */
    REMINDER("Recordatorio"),
    
    /**
     * Notificación de promoción o marketing.
     */
    PROMOTION("Promoción"),
    
    /**
     * Notificación de alerta del sistema.
     */
    SYSTEM_ALERT("Alerta del Sistema");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica si la notificación es relacionada con torneos.
     */
    public boolean isTournamentRelated() {
        return this == TOURNAMENT_REGISTRATION || 
               this == TOURNAMENT_START || 
               this == TOURNAMENT_END || 
               this == TOURNAMENT_CANCELLED;
    }
    
    /**
     * Verifica si la notificación es relacionada con tickets.
     */
    public boolean isTicketRelated() {
        return this == TICKET_CONFIRMATION || 
               this == TICKET_PAYMENT || 
               this == TICKET_EXPIRED;
    }
    
    /**
     * Verifica si la notificación es relacionada con partidas.
     */
    public boolean isMatchRelated() {
        return this == MATCH_RESULT || 
               this == MATCH_UPCOMING;
    }
} 