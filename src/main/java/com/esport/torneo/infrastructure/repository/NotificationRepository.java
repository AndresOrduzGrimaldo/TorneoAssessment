package com.esport.torneo.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esport.torneo.domain.notification.Notification;
import com.esport.torneo.domain.notification.NotificationStatus;
import com.esport.torneo.domain.notification.NotificationType;

/**
 * Repositorio para la gestión de notificaciones.
 * 
 * Proporciona operaciones de persistencia y consultas específicas para:
 * - Búsqueda por usuario y estado
 * - Notificaciones pendientes de envío
 * - Consultas por tipo y prioridad
 * - Métricas y estadísticas
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Busca todas las notificaciones de un usuario específico.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.active = true ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Busca notificaciones de un usuario por estado.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = :status AND n.active = true ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndStatus(@Param("userId") Long userId, 
                                           @Param("status") NotificationStatus status, 
                                           Pageable pageable);
    
    /**
     * Busca notificaciones pendientes de envío.
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledAt <= :now AND n.active = true ORDER BY n.priority DESC, n.scheduledAt ASC")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Busca notificaciones de alta prioridad pendientes.
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.priority IN ('HIGH', 'CRITICAL') AND n.scheduledAt <= :now AND n.active = true ORDER BY n.priority DESC, n.scheduledAt ASC")
    List<Notification> findHighPriorityPendingNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Busca notificaciones no leídas de un usuario.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status IN ('SENT', 'PENDING') AND n.active = true ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status IN ('SENT', 'PENDING') AND n.active = true")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    /**
     * Busca notificaciones por tipo.
     */
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.active = true ORDER BY n.createdAt DESC")
    Page<Notification> findByType(@Param("type") NotificationType type, Pageable pageable);
    
    /**
     * Busca notificaciones por torneo.
     */
    @Query("SELECT n FROM Notification n WHERE n.tournament.id = :tournamentId AND n.active = true ORDER BY n.createdAt DESC")
    Page<Notification> findByTournamentId(@Param("tournamentId") Long tournamentId, Pageable pageable);
    
    /**
     * Busca notificaciones por referencia.
     */
    @Query("SELECT n FROM Notification n WHERE n.referenceType = :referenceType AND n.referenceId = :referenceId AND n.active = true ORDER BY n.createdAt DESC")
    List<Notification> findByReference(@Param("referenceType") String referenceType, 
                                     @Param("referenceId") Long referenceId);
    
    /**
     * Busca notificaciones fallidas para reintento.
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.createdAt >= :since AND n.active = true ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findFailedNotificationsForRetry(@Param("since") LocalDateTime since);
    
    /**
     * Busca notificaciones programadas para un rango de fechas.
     */
    @Query("SELECT n FROM Notification n WHERE n.scheduledAt BETWEEN :start AND :end AND n.active = true ORDER BY n.scheduledAt ASC")
    List<Notification> findScheduledBetween(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    /**
     * Busca la última notificación de un tipo específico para un usuario.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.active = true ORDER BY n.createdAt DESC LIMIT 1")
    Optional<Notification> findLastByUserIdAndType(@Param("userId") Long userId, 
                                                  @Param("type") NotificationType type);
    
    /**
     * Estadísticas de notificaciones por estado.
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.createdAt >= :since AND n.active = true GROUP BY n.status")
    List<Object[]> getNotificationStatsByStatus(@Param("since") LocalDateTime since);
    
    /**
     * Estadísticas de notificaciones por tipo.
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.createdAt >= :since AND n.active = true GROUP BY n.type")
    List<Object[]> getNotificationStatsByType(@Param("since") LocalDateTime since);
    
    /**
     * Busca notificaciones expiradas (no enviadas después de cierto tiempo).
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledAt < :expiredBefore AND n.active = true")
    List<Notification> findExpiredPendingNotifications(@Param("expiredBefore") LocalDateTime expiredBefore);
    
    /**
     * Busca notificaciones por usuario y rango de fechas.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :start AND :end AND n.active = true ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndDateRange(@Param("userId") Long userId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              Pageable pageable);
    
    /**
     * Busca notificaciones masivas (mismo título y mensaje para múltiples usuarios).
     */
    @Query("SELECT n FROM Notification n WHERE n.title = :title AND n.message = :message AND n.createdAt >= :since AND n.active = true")
    List<Notification> findBulkNotifications(@Param("title") String title,
                                           @Param("message") String message,
                                           @Param("since") LocalDateTime since);
    
    /**
     * Elimina notificaciones antiguas (soft delete).
     */
    @Query("UPDATE Notification n SET n.active = false, n.deletedAt = :now WHERE n.createdAt < :before")
    int softDeleteOldNotifications(@Param("before") LocalDateTime before, @Param("now") LocalDateTime now);
    
    /**
     * Busca notificaciones duplicadas para un usuario.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.referenceId = :referenceId AND n.createdAt >= :since AND n.active = true")
    List<Notification> findDuplicateNotifications(@Param("userId") Long userId,
                                                 @Param("type") NotificationType type,
                                                 @Param("referenceId") Long referenceId,
                                                 @Param("since") LocalDateTime since);
    
    /**
     * Cuenta notificaciones por prioridad en un período.
     */
    @Query("SELECT n.priority, COUNT(n) FROM Notification n WHERE n.createdAt >= :since AND n.active = true GROUP BY n.priority")
    List<Object[]> countNotificationsByPriority(@Param("since") LocalDateTime since);
    
    /**
     * Busca notificaciones que requieren seguimiento.
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'SENT' AND n.sentAt < :followUpTime AND n.readAt IS NULL AND n.active = true")
    List<Notification> findNotificationsRequiringFollowUp(@Param("followUpTime") LocalDateTime followUpTime);
    
    /**
     * Obtiene métricas de rendimiento de notificaciones.
     */
    @Query("""
        SELECT 
            COUNT(n) as total,
            COUNT(CASE WHEN n.status = 'SENT' THEN 1 END) as sent,
            COUNT(CASE WHEN n.status = 'READ' THEN 1 END) as read,
            COUNT(CASE WHEN n.status = 'FAILED' THEN 1 END) as failed,
            AVG(CASE WHEN n.sentAt IS NOT NULL THEN EXTRACT(EPOCH FROM (n.sentAt - n.createdAt)) END) as avgDeliveryTime
        FROM Notification n 
        WHERE n.createdAt >= :since AND n.active = true
        """)
    Object[] getNotificationMetrics(@Param("since") LocalDateTime since);
} 