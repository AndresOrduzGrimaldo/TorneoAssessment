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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esport.torneo.application.dto.TicketDto;
import com.esport.torneo.application.service.TicketApplicationService;
import com.esport.torneo.application.service.TicketApplicationService.TicketStatsDto;
import com.esport.torneo.application.service.TicketApplicationService.TicketValidationDto;
import com.esport.torneo.domain.ticket.Ticket.TicketStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @Operation(summary = "Obtener ticket por ID", description = "Obtiene un ticket específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketDto> getTicketById(
            @Parameter(description = "ID del ticket") @PathVariable Long id) {
        
        logger.debug("Obteniendo ticket por ID: {}", id);
        
        try {
            TicketDto ticket = ticketApplicationService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            logger.warn("Ticket no encontrado: {}", id);
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
    @Operation(summary = "Obtener tickets de torneo", description = "Obtiene todos los tickets de un torneo específico")
    @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente")
    public ResponseEntity<Page<TicketDto>> getTournamentTickets(
            @Parameter(description = "ID del torneo") @PathVariable Long tournamentId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo tickets del torneo: {}", tournamentId);
        
        Page<TicketDto> tickets = ticketApplicationService.getTournamentTickets(tournamentId, pageable);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Obtiene tickets por estado.
     * 
     * @param status estado de los tickets
     * @param pageable información de paginación
     * @return página de tickets con el estado especificado
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener tickets por estado", description = "Obtiene tickets filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente")
    public ResponseEntity<Page<TicketDto>> getTicketsByStatus(
            @Parameter(description = "Estado del ticket") @PathVariable TicketStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo tickets por estado: {}", status);
        
        Page<TicketDto> tickets = ticketApplicationService.getTicketsByStatus(status, pageable);
        return ResponseEntity.ok(tickets);
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
    @Operation(summary = "Cancelar ticket", description = "Cancela un ticket y procesa reembolso")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket cancelado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede cancelar el ticket"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketDto> cancelTicket(
            @Parameter(description = "ID del ticket") @PathVariable Long ticketId) {
        
        logger.info("Cancelando ticket: {}", ticketId);
        
        try {
            TicketDto cancelledTicket = ticketApplicationService.cancelTicket(ticketId);
            return ResponseEntity.ok(cancelledTicket);
        } catch (IllegalArgumentException e) {
            logger.warn("Error cancelando ticket: {}", e.getMessage());
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
    @Operation(summary = "Obtener estadísticas de tickets", description = "Obtiene estadísticas generales sobre los tickets del sistema")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<TicketStatsDto> getTicketStats() {
        logger.debug("Obteniendo estadísticas de tickets");
        
        TicketStatsDto stats = ticketApplicationService.getTicketStats();
        return ResponseEntity.ok(stats);
    }
} 