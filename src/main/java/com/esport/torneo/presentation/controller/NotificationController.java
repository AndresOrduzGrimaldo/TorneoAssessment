package com.esport.torneo.presentation.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esport.torneo.application.dto.NotificationDto;
import com.esport.torneo.application.service.NotificationApplicationService;
import com.esport.torneo.domain.notification.NotificationChannel;
import com.esport.torneo.domain.notification.NotificationPriority;
import com.esport.torneo.domain.notification.NotificationType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de notificaciones.
 * 
 * Proporciona endpoints para:
 * - Consultar notificaciones de usuario
 * - Marcar notificaciones como leídas
 * - Gestión administrativa de notificaciones
 * - Estadísticas y métricas
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notificaciones", description = "API para gestión de notificaciones")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    private final NotificationApplicationService notificationService;
    
    @Autowired
    public NotificationController(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Obtiene todas las notificaciones del usuario autenticado.
     */
    @GetMapping("/my")
    @Operation(summary = "Obtener mis notificaciones", 
               description = "Obtiene todas las notificaciones del usuario autenticado con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @Parameter(description = "ID del usuario autenticado") @RequestParam Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        logger.info("Obteniendo notificaciones para usuario: {}", userId);
        
        try {
            Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al obtener notificaciones para usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene las notificaciones no leídas del usuario.
     */
    @GetMapping("/my/unread")
    @Operation(summary = "Obtener notificaciones no leídas", 
               description = "Obtiene todas las notificaciones no leídas del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones no leídas obtenidas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @Parameter(description = "ID del usuario autenticado") @RequestParam Long userId) {
        
        logger.info("Obteniendo notificaciones no leídas para usuario: {}", userId);
        
        try {
            List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al obtener notificaciones no leídas para usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cuenta las notificaciones no leídas del usuario.
     */
    @GetMapping("/my/unread/count")
    @Operation(summary = "Contar notificaciones no leídas", 
               description = "Obtiene el número de notificaciones no leídas del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> countUnreadNotifications(
            @Parameter(description = "ID del usuario autenticado") @RequestParam Long userId) {
        
        logger.debug("Contando notificaciones no leídas para usuario: {}", userId);
        
        try {
            Long count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error al contar notificaciones no leídas para usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Marca una notificación como leída.
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Marcar notificación como leída", 
               description = "Marca una notificación específica como leída")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificación marcada como leída exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado para esta notificación"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<NotificationDto> markAsRead(
            @Parameter(description = "ID de la notificación") @PathVariable Long notificationId,
            @Parameter(description = "ID del usuario autenticado") @RequestParam Long userId) {
        
        logger.info("Marcando notificación {} como leída para usuario {}", notificationId, userId);
        
        try {
            NotificationDto notification = notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al marcar notificación como leída: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error al marcar notificación {} como leída: {}", notificationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Marca múltiples notificaciones como leídas.
     */
    @PutMapping("/read-multiple")
    @Operation(summary = "Marcar múltiples notificaciones como leídas", 
               description = "Marca múltiples notificaciones como leídas en una sola operación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones marcadas como leídas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado para una o más notificaciones"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<NotificationDto>> markMultipleAsRead(
            @Parameter(description = "Lista de IDs de notificaciones") @RequestBody List<Long> notificationIds,
            @Parameter(description = "ID del usuario autenticado") @RequestParam Long userId) {
        
        logger.info("Marcando {} notificaciones como leídas para usuario {}", notificationIds.size(), userId);
        
        try {
            List<NotificationDto> notifications = notificationService.markMultipleAsRead(notificationIds, userId);
            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al marcar notificaciones como leídas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error al marcar múltiples notificaciones como leídas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ========== ENDPOINTS ADMINISTRATIVOS ==========
    
    /**
     * Obtiene notificaciones por tipo (solo administradores).
     */
    @GetMapping("/admin/by-type")
    @Operation(summary = "Obtener notificaciones por tipo", 
               description = "Obtiene notificaciones filtradas por tipo (solo administradores)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByType(
            @Parameter(description = "Tipo de notificación") @RequestParam NotificationType type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        logger.info("Obteniendo notificaciones por tipo: {}", type);
        
        try {
            Page<NotificationDto> notifications = notificationService.getNotificationsByType(type, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al obtener notificaciones por tipo {}: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene notificaciones por torneo (solo administradores).
     */
    @GetMapping("/admin/by-tournament/{tournamentId}")
    @Operation(summary = "Obtener notificaciones por torneo", 
               description = "Obtiene notificaciones relacionadas con un torneo específico (solo administradores)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long tournamentId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        logger.info("Obteniendo notificaciones para torneo: {}", tournamentId);
        
        try {
            Page<NotificationDto> notifications = notificationService.getNotificationsByTournament(tournamentId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al obtener notificaciones para torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crea notificaciones masivas (solo administradores).
     */
    @PostMapping("/admin/bulk")
    @Operation(summary = "Crear notificaciones masivas", 
               description = "Crea notificaciones para múltiples usuarios (solo administradores)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notificaciones creadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> createBulkNotifications(
            @Valid @RequestBody BulkNotificationRequest request) {
        
        logger.info("Creando {} notificaciones masivas de tipo {}", request.getUserIds().size(), request.getType());
        
        try {
            List<NotificationDto> notifications = notificationService.createBulkNotifications(
                request.getUserIds(),
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getChannel(),
                request.getPriority()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (Exception e) {
            logger.error("Error al crear notificaciones masivas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene estadísticas de notificaciones (solo administradores).
     */
    @GetMapping("/admin/stats")
    @Operation(summary = "Obtener estadísticas de notificaciones", 
               description = "Obtiene métricas y estadísticas de notificaciones (solo administradores)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationApplicationService.NotificationStatsDto> getNotificationStats(
            @Parameter(description = "Fecha desde la cual obtener estadísticas") 
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now().minusDays(30)}") LocalDateTime since) {
        
        logger.info("Obteniendo estadísticas de notificaciones desde: {}", since);
        
        try {
            NotificationApplicationService.NotificationStatsDto stats = notificationService.getNotificationStats(since);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de notificaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Limpia notificaciones antiguas (solo administradores).
     */
    @DeleteMapping("/admin/cleanup")
    @Operation(summary = "Limpiar notificaciones antiguas", 
               description = "Elimina notificaciones anteriores a un número específico de días (solo administradores)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Limpieza realizada exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> cleanupOldNotifications(
            @Parameter(description = "Número de días de antigüedad para eliminar") 
            @RequestParam(defaultValue = "90") int daysOld) {
        
        logger.info("Limpiando notificaciones anteriores a {} días", daysOld);
        
        try {
            int deletedCount = notificationService.cleanupOldNotifications(daysOld);
            return ResponseEntity.ok(deletedCount);
        } catch (Exception e) {
            logger.error("Error al limpiar notificaciones antiguas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * DTO para solicitudes de notificaciones masivas.
     */
    public static class BulkNotificationRequest {
        @Valid
        private List<Long> userIds;
        
        @Valid
        private NotificationType type;
        
        @Valid
        private String title;
        
        @Valid
        private String message;
        
        @Valid
        private NotificationChannel channel;
        
        @Valid
        private NotificationPriority priority;
        
        // Getters y setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public NotificationChannel getChannel() { return channel; }
        public void setChannel(NotificationChannel channel) { this.channel = channel; }
        
        public NotificationPriority getPriority() { return priority; }
        public void setPriority(NotificationPriority priority) { this.priority = priority; }
    }
} 