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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa una notificación en el sistema.
 * 
 * Una notificación es un mensaje que se envía a un usuario específico
 * con información relevante sobre eventos del sistema como:
 * - Confirmación de registro en torneo
 * - Inicio de torneo
 * - Resultados de partidas
 * - Actualizaciones de estado
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_created_at", columnList = "created_at")
})
public class Notification extends BaseEntity {

    /**
     * ID del usuario destinatario de la notificación.
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
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    /**
     * Título de la notificación.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200, message = "El título debe tener entre 1 y 200 caracteres")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Mensaje de la notificación.
     */
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 1, max = 1000, message = "El mensaje debe tener entre 1 y 1000 caracteres")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    /**
     * Estado de la notificación.
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    /**
     * Canal por el cual se envía la notificación.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    /**
     * Prioridad de la notificación.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private NotificationPriority priority;

    /**
     * Fecha en que se debe enviar la notificación.
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /**
     * Fecha en que se envió la notificación.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Fecha en que se leyó la notificación.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Datos adicionales en formato JSON.
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    /**
     * ID de referencia del objeto relacionado (torneo, ticket, etc).
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Tipo de referencia del objeto relacionado.
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Constructor por defecto para JPA.
     */
    protected Notification() {
        super();
    }

    /**
     * Constructor para crear una nueva notificación.
     */
    public Notification(Long userId, NotificationType type, String title, String message,
                       NotificationChannel channel, NotificationPriority priority) {
        super();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.channel = channel;
        this.priority = priority;
        this.status = NotificationStatus.PENDING;
        this.scheduledAt = LocalDateTime.now();
    }

    /**
     * Constructor completo con torneo.
     */
    public Notification(Long userId, Tournament tournament, NotificationType type, String title, String message,
                       NotificationChannel channel, NotificationPriority priority) {
        this(userId, type, title, message, channel, priority);
        this.tournament = tournament;
    }

    // ======================================================================
    // MÉTODOS DE NEGOCIO
    // ======================================================================

    /**
     * Marca la notificación como enviada.
     */
    public void markAsSent() {
        if (this.status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden marcar como enviadas las notificaciones pendientes");
        }
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marca la notificación como leída.
     */
    public void markAsRead() {
        if (this.status != NotificationStatus.SENT) {
            throw new IllegalStateException("Solo se pueden marcar como leídas las notificaciones enviadas");
        }
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Marca la notificación como fallida.
     */
    public void markAsFailed() {
        if (this.status == NotificationStatus.READ) {
            throw new IllegalStateException("No se puede marcar como fallida una notificación ya leída");
        }
        this.status = NotificationStatus.FAILED;
    }

    /**
     * Programa la notificación para una fecha específica.
     */
    public void scheduleFor(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede programar una notificación en el pasado");
        }
        this.scheduledAt = dateTime;
    }

    /**
     * Establece la referencia a un objeto relacionado.
     */
    public void setReference(String referenceType, Long referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    /**
     * Verifica si la notificación está lista para ser enviada.
     */
    public boolean isReadyToSend() {
        return status == NotificationStatus.PENDING && 
               scheduledAt != null && 
               !scheduledAt.isAfter(LocalDateTime.now());
    }

    /**
     * Verifica si la notificación es de alta prioridad.
     */
    public boolean isHighPriority() {
        return priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL;
    }

    /**
     * Obtiene el tiempo transcurrido desde la creación.
     */
    public long getMinutesSinceCreated() {
        return java.time.Duration.between(getCreatedAt(), LocalDateTime.now()).toMinutes();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", scheduledAt=" + scheduledAt +
                '}';
    }
} 