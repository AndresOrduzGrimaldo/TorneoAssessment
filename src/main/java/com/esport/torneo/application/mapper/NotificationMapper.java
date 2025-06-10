package com.esport.torneo.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.esport.torneo.application.dto.NotificationDto;
import com.esport.torneo.domain.notification.Notification;

/**
 * Mapper para convertir entre entidades Notification y NotificationDto.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Component
public class NotificationMapper {
    
    /**
     * Convierte una entidad Notification a NotificationDto.
     */
    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        
        // Información del torneo si existe
        if (notification.getTournament() != null) {
            dto.setTournamentId(notification.getTournament().getId());
            dto.setTournamentName(notification.getTournament().getName());
        }
        
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setStatus(notification.getStatus());
        dto.setChannel(notification.getChannel());
        dto.setPriority(notification.getPriority());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setSentAt(notification.getSentAt());
        dto.setReadAt(notification.getReadAt());
        dto.setMetadata(notification.getMetadata());
        dto.setReferenceId(notification.getReferenceId());
        dto.setReferenceType(notification.getReferenceType());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        
        // Campos calculados
        dto.setHighPriority(notification.isHighPriority());
        dto.setReadyToSend(notification.isReadyToSend());
        dto.setMinutesSinceCreated(notification.getMinutesSinceCreated());
        
        return dto;
    }
    
    /**
     * Convierte una lista de entidades Notification a lista de NotificationDto.
     */
    public List<NotificationDto> toDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        
        return notifications.stream()
                .map(this::toDto)
                .toList();
    }
    
    /**
     * Convierte un NotificationDto a entidad Notification.
     * Nota: Este método es principalmente para casos de actualización,
     * ya que la creación de notificaciones se hace a través de los servicios de dominio.
     */
    public Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }
        
        Notification notification = new Notification(
            dto.getUserId(),
            dto.getType(),
            dto.getTitle(),
            dto.getMessage(),
            dto.getChannel(),
            dto.getPriority()
        );
        
        // Establecer ID si existe (para actualizaciones)
        if (dto.getId() != null) {
            notification.setId(dto.getId());
        }
        
        notification.setStatus(dto.getStatus());
        notification.setScheduledAt(dto.getScheduledAt());
        notification.setSentAt(dto.getSentAt());
        notification.setReadAt(dto.getReadAt());
        notification.setMetadata(dto.getMetadata());
        notification.setReferenceId(dto.getReferenceId());
        notification.setReferenceType(dto.getReferenceType());
        
        // Los campos de auditoría se manejan automáticamente por BaseEntity
        if (dto.getCreatedAt() != null) {
            notification.setCreatedAt(dto.getCreatedAt());
        }
        if (dto.getUpdatedAt() != null) {
            notification.setUpdatedAt(dto.getUpdatedAt());
        }
        
        return notification;
    }
    
    /**
     * Actualiza una entidad Notification existente con datos de un NotificationDto.
     * Útil para operaciones de actualización parcial.
     */
    public void updateEntityFromDto(Notification notification, NotificationDto dto) {
        if (notification == null || dto == null) {
            return;
        }
        
        // Solo actualizar campos que no son null en el DTO
        if (dto.getTitle() != null) {
            notification.setTitle(dto.getTitle());
        }
        if (dto.getMessage() != null) {
            notification.setMessage(dto.getMessage());
        }
        if (dto.getStatus() != null) {
            notification.setStatus(dto.getStatus());
        }
        if (dto.getChannel() != null) {
            notification.setChannel(dto.getChannel());
        }
        if (dto.getPriority() != null) {
            notification.setPriority(dto.getPriority());
        }
        if (dto.getScheduledAt() != null) {
            notification.setScheduledAt(dto.getScheduledAt());
        }
        if (dto.getMetadata() != null) {
            notification.setMetadata(dto.getMetadata());
        }
        if (dto.getReferenceId() != null) {
            notification.setReferenceId(dto.getReferenceId());
        }
        if (dto.getReferenceType() != null) {
            notification.setReferenceType(dto.getReferenceType());
        }
    }
    
    /**
     * Crea un DTO básico con información mínima para respuestas rápidas.
     */
    public NotificationDto toBasicDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setStatus(notification.getStatus());
        dto.setPriority(notification.getPriority());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setHighPriority(notification.isHighPriority());
        
        return dto;
    }
    
    /**
     * Convierte una lista de entidades a DTOs básicos.
     */
    public List<NotificationDto> toBasicDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        
        return notifications.stream()
                .map(this::toBasicDto)
                .toList();
    }
} 