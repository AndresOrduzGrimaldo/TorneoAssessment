package com.esport.torneo.application.service;

import com.esport.torneo.application.dto.NotificationDto;
import com.esport.torneo.application.mapper.NotificationMapper;
import com.esport.torneo.domain.notification.*;
import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.infrastructure.repository.NotificationRepository;
import com.esport.torneo.infrastructure.repository.TournamentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para la gestión de notificaciones.
 * 
 * Orquesta las operaciones de notificaciones incluyendo:
 * - Creación y envío de notificaciones
 * - Consultas y filtros
 * - Gestión de estados
 * - Métricas y estadísticas
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class NotificationApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationApplicationService.class);
    
    private final NotificationRepository notificationRepository;
    private final TournamentRepository tournamentRepository;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    
    @Autowired
    public NotificationApplicationService(
            NotificationRepository notificationRepository,
            TournamentRepository tournamentRepository,
            NotificationService notificationService,
            NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.tournamentRepository = tournamentRepository;
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }
    
    /**
     * Crea una notificación de registro en torneo.
     */
    public NotificationDto createTournamentRegistrationNotification(Long userId, Long tournamentId) {
        logger.info("Creando notificación de registro en torneo para usuario {} y torneo {}", userId, tournamentId);
        
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));
        
        // Verificar si ya existe una notificación similar reciente
        Optional<Notification> existingNotification = notificationRepository
                .findLastByUserIdAndType(userId, NotificationType.TOURNAMENT_REGISTRATION);
        
        if (existingNotification.isPresent() && 
            existingNotification.get().getReferenceId().equals(tournamentId) &&
            existingNotification.get().getCreatedAt().isAfter(LocalDateTime.now().minusHours(1))) {
            logger.warn("Notificación de registro duplicada detectada para usuario {} y torneo {}", userId, tournamentId);
            return notificationMapper.toDto(existingNotification.get());
        }
        
        Notification notification = notificationService.createTournamentRegistrationNotification(userId, tournament);
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación de registro creada con ID: {}", savedNotification.getId());
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Crea una notificación de inicio de torneo.
     */
    public NotificationDto createTournamentStartNotification(Long userId, Long tournamentId) {
        logger.info("Creando notificación de inicio de torneo para usuario {} y torneo {}", userId, tournamentId);
        
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));
        
        Notification notification = notificationService.createTournamentStartNotification(userId, tournament);
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación de inicio de torneo creada con ID: {}", savedNotification.getId());
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Crea una notificación de confirmación de ticket.
     */
    public NotificationDto createTicketConfirmationNotification(Long userId, Long ticketId, String qrCode) {
        logger.info("Creando notificación de confirmación de ticket para usuario {} y ticket {}", userId, ticketId);
        
        Notification notification = notificationService.createTicketConfirmationNotification(userId, ticketId, qrCode);
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación de confirmación de ticket creada con ID: {}", savedNotification.getId());
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Crea una notificación de pago de ticket.
     */
    public NotificationDto createTicketPaymentNotification(Long userId, Long ticketId, Double amount) {
        logger.info("Creando notificación de pago de ticket para usuario {} y ticket {}", userId, ticketId);
        
        Notification notification = notificationService.createTicketPaymentNotification(userId, ticketId, amount);
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación de pago de ticket creada con ID: {}", savedNotification.getId());
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Obtiene todas las notificaciones de un usuario.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        logger.debug("Obteniendo notificaciones para usuario: {}", userId);
        
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Obtiene las notificaciones no leídas de un usuario.
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        logger.debug("Obteniendo notificaciones no leídas para usuario: {}", userId);
        
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * Marca una notificación como leída.
     */
    public NotificationDto markAsRead(Long notificationId, Long userId) {
        logger.info("Marcando notificación {} como leída para usuario {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + notificationId));
        
        // Verificar que la notificación pertenece al usuario
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("La notificación no pertenece al usuario especificado");
        }
        
        notification.markAsRead();
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación {} marcada como leída", notificationId);
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Marca múltiples notificaciones como leídas.
     */
    public List<NotificationDto> markMultipleAsRead(List<Long> notificationIds, Long userId) {
        logger.info("Marcando {} notificaciones como leídas para usuario {}", notificationIds.size(), userId);
        
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        
        // Verificar que todas las notificaciones pertenecen al usuario
        notifications.forEach(notification -> {
            if (!notification.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Una o más notificaciones no pertenecen al usuario especificado");
            }
            notification.markAsRead();
        });
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        
        logger.info("{} notificaciones marcadas como leídas", savedNotifications.size());
        return savedNotifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }
    
    /**
     * Obtiene notificaciones pendientes de envío.
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getPendingNotifications() {
        logger.debug("Obteniendo notificaciones pendientes de envío");
        
        List<Notification> notifications = notificationRepository.findPendingNotifications(LocalDateTime.now());
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }
    
    /**
     * Obtiene notificaciones de alta prioridad pendientes.
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getHighPriorityPendingNotifications() {
        logger.debug("Obteniendo notificaciones de alta prioridad pendientes");
        
        List<Notification> notifications = notificationRepository.findHighPriorityPendingNotifications(LocalDateTime.now());
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }
    
    /**
     * Marca una notificación como enviada.
     */
    public NotificationDto markAsSent(Long notificationId) {
        logger.info("Marcando notificación {} como enviada", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + notificationId));
        
        notification.markAsSent();
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Notificación {} marcada como enviada", notificationId);
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Marca una notificación como fallida.
     */
    public NotificationDto markAsFailed(Long notificationId) {
        logger.warn("Marcando notificación {} como fallida", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + notificationId));
        
        notification.markAsFailed();
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.warn("Notificación {} marcada como fallida", notificationId);
        return notificationMapper.toDto(savedNotification);
    }
    
    /**
     * Obtiene notificaciones por tipo.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByType(NotificationType type, Pageable pageable) {
        logger.debug("Obteniendo notificaciones por tipo: {}", type);
        
        Page<Notification> notifications = notificationRepository.findByType(type, pageable);
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Obtiene notificaciones por torneo.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByTournament(Long tournamentId, Pageable pageable) {
        logger.debug("Obteniendo notificaciones para torneo: {}", tournamentId);
        
        Page<Notification> notifications = notificationRepository.findByTournamentId(tournamentId, pageable);
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Crea notificaciones masivas.
     */
    public List<NotificationDto> createBulkNotifications(List<Long> userIds, NotificationType type, 
                                                        String title, String message, 
                                                        NotificationChannel channel, 
                                                        NotificationPriority priority) {
        logger.info("Creando {} notificaciones masivas de tipo {}", userIds.size(), type);
        
        List<Notification> notifications = notificationService.createBulkNotifications(
                userIds, type, title, message, channel, priority);
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        
        logger.info("{} notificaciones masivas creadas", savedNotifications.size());
        return savedNotifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }
    
    /**
     * Obtiene estadísticas de notificaciones.
     */
    @Transactional(readOnly = true)
    public NotificationStatsDto getNotificationStats(LocalDateTime since) {
        logger.debug("Obteniendo estadísticas de notificaciones desde: {}", since);
        
        Object[] metrics = notificationRepository.getNotificationMetrics(since);
        List<Object[]> statsByStatus = notificationRepository.getNotificationStatsByStatus(since);
        List<Object[]> statsByType = notificationRepository.getNotificationStatsByType(since);
        List<Object[]> statsByPriority = notificationRepository.countNotificationsByPriority(since);
        
        return NotificationStatsDto.builder()
                .total(((Number) metrics[0]).longValue())
                .sent(((Number) metrics[1]).longValue())
                .read(((Number) metrics[2]).longValue())
                .failed(((Number) metrics[3]).longValue())
                .averageDeliveryTime(metrics[4] != null ? ((Number) metrics[4]).doubleValue() : 0.0)
                .statsByStatus(statsByStatus)
                .statsByType(statsByType)
                .statsByPriority(statsByPriority)
                .build();
    }
    
    /**
     * Limpia notificaciones antiguas.
     */
    public int cleanupOldNotifications(int daysOld) {
        logger.info("Limpiando notificaciones anteriores a {} días", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.softDeleteOldNotifications(cutoffDate, LocalDateTime.now());
        
        logger.info("{} notificaciones antiguas eliminadas", deletedCount);
        return deletedCount;
    }
    
    /**
     * DTO para estadísticas de notificaciones.
     */
    public static class NotificationStatsDto {
        private Long total;
        private Long sent;
        private Long read;
        private Long failed;
        private Double averageDeliveryTime;
        private List<Object[]> statsByStatus;
        private List<Object[]> statsByType;
        private List<Object[]> statsByPriority;
        
        // Constructor, getters, setters y builder
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private NotificationStatsDto stats = new NotificationStatsDto();
            
            public Builder total(Long total) {
                stats.total = total;
                return this;
            }
            
            public Builder sent(Long sent) {
                stats.sent = sent;
                return this;
            }
            
            public Builder read(Long read) {
                stats.read = read;
                return this;
            }
            
            public Builder failed(Long failed) {
                stats.failed = failed;
                return this;
            }
            
            public Builder averageDeliveryTime(Double averageDeliveryTime) {
                stats.averageDeliveryTime = averageDeliveryTime;
                return this;
            }
            
            public Builder statsByStatus(List<Object[]> statsByStatus) {
                stats.statsByStatus = statsByStatus;
                return this;
            }
            
            public Builder statsByType(List<Object[]> statsByType) {
                stats.statsByType = statsByType;
                return this;
            }
            
            public Builder statsByPriority(List<Object[]> statsByPriority) {
                stats.statsByPriority = statsByPriority;
                return this;
            }
            
            public NotificationStatsDto build() {
                return stats;
            }
        }
        
        // Getters
        public Long getTotal() { return total; }
        public Long getSent() { return sent; }
        public Long getRead() { return read; }
        public Long getFailed() { return failed; }
        public Double getAverageDeliveryTime() { return averageDeliveryTime; }
        public List<Object[]> getStatsByStatus() { return statsByStatus; }
        public List<Object[]> getStatsByType() { return statsByType; }
        public List<Object[]> getStatsByPriority() { return statsByPriority; }
    }
} 