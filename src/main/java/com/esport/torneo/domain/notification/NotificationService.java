package com.esport.torneo.domain.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.esport.torneo.domain.tournament.Tournament;

/**
 * Servicio de dominio para la gestión de notificaciones.
 * 
 * Contiene la lógica de negocio para:
 * - Creación de notificaciones específicas por tipo
 * - Validación de reglas de negocio
 * - Cálculo de prioridades
 * - Programación de envíos
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Service
public class NotificationService {
    
    /**
     * Crea una notificación de registro en torneo.
     */
    public Notification createTournamentRegistrationNotification(Long userId, Tournament tournament) {
        String title = "Registro confirmado en torneo";
        String message = String.format("Te has registrado exitosamente en el torneo '%s'. " +
                "El torneo comenzará el %s.", 
                tournament.getName(), 
                tournament.getStartDate().toString());
        
        Notification notification = new Notification(
            userId, 
            tournament,
            NotificationType.TOURNAMENT_REGISTRATION,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.NORMAL
        );
        
        notification.setReference("TOURNAMENT", tournament.getId());
        return notification;
    }
    
    /**
     * Crea una notificación de inicio de torneo.
     */
    public Notification createTournamentStartNotification(Long userId, Tournament tournament) {
        String title = "¡El torneo está comenzando!";
        String message = String.format("El torneo '%s' está comenzando ahora. " +
                "¡Prepárate para la competencia!", tournament.getName());
        
        Notification notification = new Notification(
            userId,
            tournament,
            NotificationType.TOURNAMENT_START,
            title,
            message,
            NotificationChannel.WEBSOCKET,
            NotificationPriority.HIGH
        );
        
        notification.setReference("TOURNAMENT", tournament.getId());
        notification.scheduleFor(tournament.getStartDate());
        return notification;
    }
    
    /**
     * Crea una notificación de finalización de torneo.
     */
    public Notification createTournamentEndNotification(Long userId, Tournament tournament, String result) {
        String title = "Torneo finalizado";
        String message = String.format("El torneo '%s' ha finalizado. %s", 
                tournament.getName(), result);
        
        Notification notification = new Notification(
            userId,
            tournament,
            NotificationType.TOURNAMENT_END,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.NORMAL
        );
        
        notification.setReference("TOURNAMENT", tournament.getId());
        return notification;
    }
    
    /**
     * Crea una notificación de cancelación de torneo.
     */
    public Notification createTournamentCancelledNotification(Long userId, Tournament tournament, String reason) {
        String title = "Torneo cancelado";
        String message = String.format("Lamentamos informarte que el torneo '%s' ha sido cancelado. " +
                "Motivo: %s", tournament.getName(), reason);
        
        Notification notification = new Notification(
            userId,
            tournament,
            NotificationType.TOURNAMENT_CANCELLED,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.HIGH
        );
        
        notification.setReference("TOURNAMENT", tournament.getId());
        return notification;
    }
    
    /**
     * Crea una notificación de confirmación de ticket.
     */
    public Notification createTicketConfirmationNotification(Long userId, Long ticketId, String qrCode) {
        String title = "Ticket confirmado";
        String message = "Tu ticket ha sido confirmado exitosamente. " +
                "Guarda tu código QR para el acceso al evento.";
        
        Notification notification = new Notification(
            userId,
            NotificationType.TICKET_CONFIRMATION,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.NORMAL
        );
        
        notification.setReference("TICKET", ticketId);
        notification.setMetadata(String.format("{\"qrCode\":\"%s\"}", qrCode));
        return notification;
    }
    
    /**
     * Crea una notificación de pago de ticket.
     */
    public Notification createTicketPaymentNotification(Long userId, Long ticketId, Double amount) {
        String title = "Pago procesado";
        String message = String.format("Tu pago de $%.2f ha sido procesado exitosamente. " +
                "Tu ticket está confirmado.", amount);
        
        Notification notification = new Notification(
            userId,
            NotificationType.TICKET_PAYMENT,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.NORMAL
        );
        
        notification.setReference("TICKET", ticketId);
        notification.setMetadata(String.format("{\"amount\":%.2f}", amount));
        return notification;
    }
    
    /**
     * Crea una notificación de expiración de ticket.
     */
    public Notification createTicketExpiredNotification(Long userId, Long ticketId) {
        String title = "Ticket expirado";
        String message = "Tu ticket ha expirado. Si necesitas un nuevo ticket, " +
                "por favor realiza una nueva reserva.";
        
        Notification notification = new Notification(
            userId,
            NotificationType.TICKET_EXPIRED,
            title,
            message,
            NotificationChannel.EMAIL,
            NotificationPriority.LOW
        );
        
        notification.setReference("TICKET", ticketId);
        return notification;
    }
    
    /**
     * Crea una notificación de recordatorio.
     */
    public Notification createReminderNotification(Long userId, String title, String message, 
                                                  LocalDateTime scheduledFor) {
        Notification notification = new Notification(
            userId,
            NotificationType.REMINDER,
            title,
            message,
            NotificationChannel.WEBSOCKET,
            NotificationPriority.NORMAL
        );
        
        notification.scheduleFor(scheduledFor);
        return notification;
    }
    
    /**
     * Crea una notificación de alerta del sistema.
     */
    public Notification createSystemAlertNotification(Long userId, String title, String message) {
        Notification notification = new Notification(
            userId,
            NotificationType.SYSTEM_ALERT,
            title,
            message,
            NotificationChannel.WEBSOCKET,
            NotificationPriority.CRITICAL
        );
        
        return notification;
    }
    
    /**
     * Determina el canal óptimo para una notificación basado en su tipo y prioridad.
     */
    public NotificationChannel determineOptimalChannel(NotificationType type, NotificationPriority priority) {
        // Notificaciones críticas siempre por WebSocket para inmediatez
        if (priority == NotificationPriority.CRITICAL) {
            return NotificationChannel.WEBSOCKET;
        }
        
        // Notificaciones de alta prioridad por WebSocket o Push
        if (priority == NotificationPriority.HIGH) {
            return type.isTournamentRelated() ? NotificationChannel.WEBSOCKET : NotificationChannel.PUSH;
        }
        
        // Notificaciones de tickets por email para tener registro
        if (type.isTicketRelated()) {
            return NotificationChannel.EMAIL;
        }
        
        // Por defecto, email para notificaciones normales
        return NotificationChannel.EMAIL;
    }
    
    /**
     * Calcula la prioridad de una notificación basada en su tipo y contexto.
     */
    public NotificationPriority calculatePriority(NotificationType type, Tournament tournament) {
        // Cancelaciones y alertas son críticas
        if (type == NotificationType.TOURNAMENT_CANCELLED || type == NotificationType.SYSTEM_ALERT) {
            return NotificationPriority.CRITICAL;
        }
        
        // Inicio de torneo es alta prioridad
        if (type == NotificationType.TOURNAMENT_START) {
            return NotificationPriority.HIGH;
        }
        
        // Notificaciones de pago son alta prioridad
        if (type == NotificationType.TICKET_PAYMENT) {
            return NotificationPriority.HIGH;
        }
        
        // Expiración de tickets es baja prioridad
        if (type == NotificationType.TICKET_EXPIRED) {
            return NotificationPriority.LOW;
        }
        
        // Por defecto, prioridad normal
        return NotificationPriority.NORMAL;
    }
    
    /**
     * Valida si una notificación puede ser enviada.
     */
    public boolean canSendNotification(Notification notification) {
        if (notification == null) {
            return false;
        }
        
        // Verificar que tenga los campos obligatorios
        if (notification.getUserId() == null || 
            notification.getType() == null ||
            notification.getTitle() == null || notification.getTitle().trim().isEmpty() ||
            notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            return false;
        }
        
        // Verificar que esté en estado correcto
        if (notification.getStatus() != NotificationStatus.PENDING) {
            return false;
        }
        
        // Verificar que esté programada para ahora o antes
        return notification.isReadyToSend();
    }
    
    /**
     * Crea notificaciones masivas para una lista de usuarios.
     */
    public List<Notification> createBulkNotifications(List<Long> userIds, NotificationType type, 
                                                     String title, String message, 
                                                     NotificationChannel channel, 
                                                     NotificationPriority priority) {
        return userIds.stream()
                .map(userId -> new Notification(userId, type, title, message, channel, priority))
                .toList();
    }
} 