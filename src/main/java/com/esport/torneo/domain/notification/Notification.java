package com.esport.torneo.domain.notification;

import java.time.LocalDateTime;

import com.esport.torneo.domain.common.BaseEntity;
import com.esport.torneo.domain.tournament.Tournament;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa una notificación en el sistema.
 * 
 * Las notificaciones pueden ser de diferentes tipos:
 * - EMAIL: Notificaciones por correo electrónico
 * - WEBSOCKET: Notificaciones en tiempo real
 * - PUSH: Notificaciones push móviles
 * 
 * Incluye funcionalidad para:
 * - Gestión de estados de envío
 * - Reintentos automáticos
 * - Seguimiento de lectura
 * - Manejo de errores
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_tournament", columnList = "tournament_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
public class Notification extends BaseEntity {

    /**
     * Tipos de notificación disponibles.
     */
    public enum NotificationType {
        EMAIL("Email"),
        WEBSOCKET("WebSocket"),
        PUSH("Push");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Estados posibles de una notificación.
     */
    public enum NotificationStatus {
        PENDING("Pendiente"),
        SENT("Enviada"),
        FAILED("Fallida"),
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
    }

    /**
     * ID del usuario destinatario.
     */
    @NotNull(message = "El usuario es obligatorio")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Torneo relacionado (opcional).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    /**
     * Tipo de notificación.
     */
    @NotNull(message = "El tipo de notificación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    /**
     * Asunto de la notificación.
     */
    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 1, max = 255, message = "El asunto debe tener entre 1 y 255 caracteres")
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    /**
     * Mensaje de la notificación.
     */
    @NotBlank(message = "El mensaje es obligatorio")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Estado actual de la notificación.
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Fecha y hora de envío.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Fecha y hora de lectura.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Número de reintentos realizados.
     */
    @Min(value = 0, message = "El número de reintentos no puede ser negativo")
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * Mensaje de error en caso de fallo.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Constructor por defecto para JPA.
     */
    protected Notification() {
        super();
    }

    /**
     * Constructor para crear una nueva notificación.
     * 
     * @param userId el ID del usuario destinatario
     * @param type el tipo de notificación
     * @param subject el asunto
     * @param message el mensaje
     */
    public Notification(Long userId, NotificationType type, String subject, String message) {
        super();
        this.userId = userId;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * Constructor completo con torneo.
     * 
     * @param userId el ID del usuario destinatario
     * @param tournament el torneo relacionado
     * @param type el tipo de notificación
     * @param subject el asunto
     * @param message el mensaje
     */
    public Notification(Long userId, Tournament tournament, NotificationType type, 
                       String subject, String message) {
        this(userId, type, subject, message);
        this.tournament = tournament;
    }

    // ======================================================================
    // MÉTODOS DE NEGOCIO
    // ======================================================================

    /**
     * Marca la notificación como enviada.
     * 
     * @throws IllegalStateException si la notificación no puede ser marcada como enviada
     */
    public void markAsSent() {
        if (!status.canTransitionTo(NotificationStatus.SENT)) {
            throw new IllegalStateException("No se puede marcar como enviada la notificación en estado: " + status);
        }
        
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null; // Limpiar errores previos
    }

    /**
     * Marca la notificación como fallida.
     * 
     * @param errorMessage el mensaje de error
     * @throws IllegalStateException si la notificación no puede ser marcada como fallida
     */
    public void markAsFailed(String errorMessage) {
        if (!status.canTransitionTo(NotificationStatus.FAILED)) {
            throw new IllegalStateException("No se puede marcar como fallida la notificación en estado: " + status);
        }
        
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * Marca la notificación como leída.
     * 
     * @throws IllegalStateException si la notificación no puede ser marcada como leída
     */
    public void markAsRead() {
        if (!status.canTransitionTo(NotificationStatus.READ)) {
            throw new IllegalStateException("No se puede marcar como leída la notificación en estado: " + status);
        }
        
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Reinicia la notificación para un nuevo intento.
     * 
     * @throws IllegalStateException si la notificación no puede ser reiniciada
     */
    public void resetForRetry() {
        if (!status.canTransitionTo(NotificationStatus.PENDING)) {
            throw new IllegalStateException("No se puede reiniciar la notificación en estado: " + status);
        }
        
        this.status = NotificationStatus.PENDING;
        this.errorMessage = null;
    }

    /**
     * Verifica si la notificación puede ser reenviada.
     * 
     * @param maxRetries el número máximo de reintentos permitidos
     * @return true si puede ser reenviada
     */
    public boolean canRetry(int maxRetries) {
        return status == NotificationStatus.FAILED && retryCount < maxRetries;
    }

    /**
     * Verifica si la notificación está pendiente de envío.
     * 
     * @return true si está pendiente
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }

    /**
     * Verifica si la notificación fue enviada exitosamente.
     * 
     * @return true si fue enviada
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }

    /**
     * Verifica si la notificación falló en el envío.
     * 
     * @return true si falló
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    /**
     * Verifica si la notificación fue leída.
     * 
     * @return true si fue leída
     */
    public boolean isRead() {
        return status == NotificationStatus.READ;
    }

    /**
     * Calcula el tiempo transcurrido desde el envío.
     * 
     * @return el tiempo en minutos, o null si no ha sido enviada
     */
    public Long getMinutesSinceSent() {
        if (sentAt == null) {
            return null;
        }
        
        return java.time.Duration.between(sentAt, LocalDateTime.now()).toMinutes();
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", tournamentId=" + (tournament != null ? tournament.getId() : null) +
                ", type=" + type +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", retryCount=" + retryCount +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
} 