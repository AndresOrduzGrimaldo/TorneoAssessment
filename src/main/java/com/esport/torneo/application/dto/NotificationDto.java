package com.esport.torneo.application.dto;

import java.time.LocalDateTime;

import com.esport.torneo.domain.notification.NotificationChannel;
import com.esport.torneo.domain.notification.NotificationPriority;
import com.esport.torneo.domain.notification.NotificationStatus;
import com.esport.torneo.domain.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferir datos de notificaciones.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public class NotificationDto {
    
    private Long id;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;
    
    private Long tournamentId;
    private String tournamentName;
    
    @NotNull(message = "El tipo de notificación es obligatorio")
    private NotificationType type;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200, message = "El título debe tener entre 1 y 200 caracteres")
    private String title;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 1, max = 1000, message = "El mensaje debe tener entre 1 y 1000 caracteres")
    private String message;
    
    @NotNull(message = "El estado es obligatorio")
    private NotificationStatus status;
    
    @NotNull(message = "El canal es obligatorio")
    private NotificationChannel channel;
    
    @NotNull(message = "La prioridad es obligatoria")
    private NotificationPriority priority;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;
    
    private String metadata;
    
    private Long referenceId;
    private String referenceType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Campos calculados
    private boolean isHighPriority;
    private boolean isReadyToSend;
    private long minutesSinceCreated;
    
    /**
     * Constructor por defecto.
     */
    public NotificationDto() {
    }
    
    /**
     * Constructor completo.
     */
    public NotificationDto(Long id, Long userId, Long tournamentId, String tournamentName,
                          NotificationType type, String title, String message,
                          NotificationStatus status, NotificationChannel channel,
                          NotificationPriority priority, LocalDateTime scheduledAt,
                          LocalDateTime sentAt, LocalDateTime readAt, String metadata,
                          Long referenceId, String referenceType, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = status;
        this.channel = channel;
        this.priority = priority;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.metadata = metadata;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Calcular campos derivados
        this.isHighPriority = priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL;
        this.isReadyToSend = status == NotificationStatus.PENDING && 
                           scheduledAt != null && 
                           !scheduledAt.isAfter(LocalDateTime.now());
        this.minutesSinceCreated = createdAt != null ? 
                java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes() : 0;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getTournamentId() {
        return tournamentId;
    }
    
    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }
    
    public String getTournamentName() {
        return tournamentName;
    }
    
    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
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
        // Recalcular isHighPriority cuando se actualiza la prioridad
        this.isHighPriority = priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL;
    }
    
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
        // Recalcular isReadyToSend cuando se actualiza scheduledAt
        this.isReadyToSend = status == NotificationStatus.PENDING && 
                           scheduledAt != null && 
                           !scheduledAt.isAfter(LocalDateTime.now());
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        // Recalcular minutesSinceCreated cuando se actualiza createdAt
        this.minutesSinceCreated = createdAt != null ? 
                java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes() : 0;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isHighPriority() {
        return isHighPriority;
    }
    
    public void setHighPriority(boolean highPriority) {
        isHighPriority = highPriority;
    }
    
    public boolean isReadyToSend() {
        return isReadyToSend;
    }
    
    public void setReadyToSend(boolean readyToSend) {
        isReadyToSend = readyToSend;
    }
    
    public long getMinutesSinceCreated() {
        return minutesSinceCreated;
    }
    
    public void setMinutesSinceCreated(long minutesSinceCreated) {
        this.minutesSinceCreated = minutesSinceCreated;
    }
    
    /**
     * Verifica si la notificación está leída.
     */
    public boolean isRead() {
        return status == NotificationStatus.READ;
    }
    
    /**
     * Verifica si la notificación está enviada.
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT || status == NotificationStatus.READ;
    }
    
    /**
     * Verifica si la notificación falló.
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }
    
    /**
     * Verifica si la notificación está pendiente.
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }
    
    /**
     * Obtiene el nombre del tipo de notificación.
     */
    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : null;
    }
    
    /**
     * Obtiene el nombre del estado de notificación.
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }
    
    /**
     * Obtiene el nombre del canal de notificación.
     */
    public String getChannelDisplayName() {
        return channel != null ? channel.getDisplayName() : null;
    }
    
    /**
     * Obtiene el nombre de la prioridad de notificación.
     */
    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : null;
    }
    
    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", scheduledAt=" + scheduledAt +
                ", createdAt=" + createdAt +
                '}';
    }
} 