package com.esport.torneo.presentation.controller;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esport.torneo.application.dto.TicketDto;
import com.esport.torneo.application.service.TicketApplicationService;
import com.esport.torneo.application.service.TicketApplicationService.TicketStatsDto;
import com.esport.torneo.application.service.TicketApplicationService.TicketValidationDto;
import com.esport.torneo.domain.ticket.Ticket.TicketStatus;
import com.esport.torneo.infrastructure.config.RedisConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

/**
 * Controlador REST para la gestión de tickets.
 * 
 * Proporciona endpoints para operaciones de tickets virtuales
 * incluyendo creación, validación, uso y consultas.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "API para gestión de tickets virtuales")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketApplicationService ticketApplicationService;

    /**
     * Constructor del controlador.
     * 
     * @param ticketApplicationService servicio de aplicación de tickets
     */
    public TicketController(TicketApplicationService ticketApplicationService) {
        this.ticketApplicationService = ticketApplicationService;
    }

    /**
     * Crea un nuevo ticket para un torneo.
     * 
     * @param tournamentId ID del torneo
     * @param userId ID del usuario
     * @param price precio del ticket (opcional, se toma del torneo si no se especifica)
     * @return el ticket creado
     */
    @PostMapping
    @Operation(summary = "Crear nuevo ticket", description = "Crea un nuevo ticket virtual para un torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado"),
        @ApiResponse(responseCode = "409", description = "No se puede crear ticket para este torneo")
    })
    public ResponseEntity<TicketDto> createTicket(
            @Parameter(description = "ID del torneo") @RequestParam Long tournamentId,
            @Parameter(description = "ID del usuario") @RequestParam Long userId,
            @Parameter(description = "Precio del ticket") @RequestParam(required = false) BigDecimal price) {
        
        logger.info("Creando ticket para torneo {} y usuario {}", tournamentId, userId);
        
        try {
            // Si no se especifica precio, usar BigDecimal.ZERO por defecto
            BigDecimal ticketPrice = price != null ? price : BigDecimal.ZERO;
            
            TicketDto createdTicket = ticketApplicationService.createTicket(tournamentId, userId, ticketPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
        } catch (IllegalArgumentException e) {
            logger.warn("Error creando ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene un ticket por ID.
     * 
     * @param id ID del ticket
     * @return el ticket encontrado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get ticket by ID",
        description = "Retrieve a specific ticket by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket found",
                    content = @Content(schema = @Schema(implementation = TicketDto.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.TICKETS_CACHE, key = "'by_id_' + #id")
    public ResponseEntity<TicketDto> getTicketById(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable Long id) {
        try {
            TicketDto ticket = ticketApplicationService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene un ticket por código.
     * 
     * @param code código del ticket
     * @return el ticket encontrado
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener ticket por código", description = "Obtiene un ticket específico por su código único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketDto> getTicketByCode(
            @Parameter(description = "Código del ticket") @PathVariable String code) {
        
        logger.debug("Obteniendo ticket por código: {}", code);
        
        try {
            TicketDto ticket = ticketApplicationService.getTicketByCode(code);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            logger.warn("Ticket no encontrado: {}", code);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene tickets de un usuario.
     * 
     * @param userId ID del usuario
     * @param pageable información de paginación
     * @return página de tickets del usuario
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener tickets de usuario", description = "Obtiene todos los tickets de un usuario específico")
    @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente")
    public ResponseEntity<Page<TicketDto>> getUserTickets(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo tickets del usuario: {}", userId);
        
        Page<TicketDto> tickets = ticketApplicationService.getUserTickets(userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Obtiene tickets de un torneo.
     * 
     * @param tournamentId ID del torneo
     * @param pageable información de paginación
     * @return página de tickets del torneo
     */
    @GetMapping("/tournament/{tournamentId}")
    @Operation(
        summary = "Get tickets by tournament",
        description = "Retrieve tickets for a specific tournament. Requires ORGANIZER or ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TicketDto>> getTicketsByTournament(
            @Parameter(description = "Tournament ID", required = true)
            @PathVariable Long tournamentId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        List<TicketDto> tickets = ticketApplicationService.getTicketsByTournament(tournamentId);
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tickets.size());
        Page<TicketDto> ticketsPage = new PageImpl<>(
            tickets.subList(start, end), 
            pageable, 
            tickets.size()
        );
        
        return ResponseEntity.ok(ticketsPage);
    }

    /**
     * Obtiene tickets por estado.
     * 
     * @param status estado de los tickets
     * @param pageable información de paginación
     * @return página de tickets con el estado especificado
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get tickets by status",
        description = "Retrieve tickets filtered by status. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TicketDto>> getTicketsByStatus(
            @Parameter(description = "Ticket status", required = true)
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            List<TicketDto> tickets = ticketApplicationService.getTicketsByStatus(ticketStatus);
            
            // Create a page from the list
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), tickets.size());
            Page<TicketDto> ticketsPage = new PageImpl<>(
                tickets.subList(start, end), 
                pageable, 
                tickets.size()
            );
            
            return ResponseEntity.ok(ticketsPage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marca un ticket como pagado.
     * 
     * @param ticketId ID del ticket
     * @param paymentReference referencia del pago
     * @return el ticket actualizado
     */
    @PutMapping("/{ticketId}/pay")
    @Operation(summary = "Marcar ticket como pagado", description = "Marca un ticket como pagado con referencia de pago")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket marcado como pagado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede marcar como pagado"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketDto> markTicketAsPaid(
            @Parameter(description = "ID del ticket") @PathVariable Long ticketId,
            @Parameter(description = "Referencia del pago") @RequestParam String paymentReference) {
        
        logger.info("Marcando ticket como pagado: {}", ticketId);
        
        try {
            TicketDto updatedTicket = ticketApplicationService.markTicketAsPaid(ticketId, paymentReference);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            logger.warn("Error marcando ticket como pagado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Usa un ticket (marca como usado).
     * 
     * @param ticketCode código del ticket
     * @return el ticket actualizado
     */
    @PutMapping("/use/{ticketCode}")
    @Operation(summary = "Usar ticket", description = "Marca un ticket como usado (para ingreso al evento)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket usado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Ticket no válido para uso"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketDto> useTicket(
            @Parameter(description = "Código del ticket") @PathVariable String ticketCode) {
        
        logger.info("Usando ticket: {}", ticketCode);
        
        try {
            TicketDto usedTicket = ticketApplicationService.useTicket(ticketCode);
            return ResponseEntity.ok(usedTicket);
        } catch (IllegalArgumentException e) {
            logger.warn("Error usando ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancela un ticket.
     * 
     * @param ticketId ID del ticket
     * @return el ticket cancelado
     */
    @PutMapping("/{ticketId}/cancel")
    @Operation(
        summary = "Cancel ticket",
        description = "Cancel a ticket. Users can cancel their own tickets, organizers and admins can cancel any ticket."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket cancelled successfully",
                    content = @Content(schema = @Schema(implementation = TicketDto.class))),
        @ApiResponse(responseCode = "400", description = "Ticket cannot be cancelled"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = RedisConfig.TICKETS_CACHE, allEntries = true)
    public ResponseEntity<TicketDto> cancelTicket(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable Long ticketId,
            Authentication authentication) {
        try {
            // Check if user owns the ticket or has admin/organizer role
            if (!canCancelTicket(ticketId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            TicketDto ticket = ticketApplicationService.cancelTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Valida un ticket por código QR.
     * 
     * @param ticketCode código del ticket a validar
     * @return información de validación del ticket
     */
    @GetMapping("/validate/{ticketCode}")
    @Operation(summary = "Validar ticket", description = "Valida un ticket por código QR para verificar su estado")
    @ApiResponse(responseCode = "200", description = "Validación completada")
    public ResponseEntity<TicketValidationDto> validateTicket(
            @Parameter(description = "Código del ticket") @PathVariable String ticketCode) {
        
        logger.debug("Validando ticket: {}", ticketCode);
        
        TicketValidationDto validation = ticketApplicationService.validateTicket(ticketCode);
        return ResponseEntity.ok(validation);
    }

    /**
     * Procesa tickets expirados automáticamente.
     * 
     * @return número de tickets marcados como expirados
     */
    @PostMapping("/process-expired")
    @Operation(summary = "Procesar tickets expirados", description = "Proceso automático para marcar tickets expirados")
    @ApiResponse(responseCode = "200", description = "Proceso completado exitosamente")
    public ResponseEntity<Integer> processExpiredTickets() {
        logger.info("Procesando tickets expirados");
        
        int expiredCount = ticketApplicationService.markExpiredTickets();
        logger.info("Procesados {} tickets expirados", expiredCount);
        
        return ResponseEntity.ok(expiredCount);
    }

    /**
     * Obtiene estadísticas de tickets.
     * 
     * @return estadísticas generales de tickets
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get ticket statistics",
        description = "Get statistics about tickets. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getTicketStats() {
        Object stats = ticketApplicationService.getTicketStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Extract user ID from authentication context.
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // This is a simplified implementation
        // In a real application, you would extract the user ID from the JWT token
        // or from the principal object
        return 1L; // Placeholder - implement proper user ID extraction
    }

    /**
     * Check if the authenticated user can cancel the ticket.
     */
    private boolean canCancelTicket(Long ticketId, Authentication authentication) {
        // Check if user has admin or organizer role
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                 auth.getAuthority().equals("ROLE_ORGANIZER"))) {
            return true;
        }
        
        // Check if user owns the ticket
        try {
            TicketDto ticket = ticketApplicationService.getTicketById(ticketId);
            Long currentUserId = extractUserIdFromAuthentication(authentication);
            return ticket.getUserId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }
} 