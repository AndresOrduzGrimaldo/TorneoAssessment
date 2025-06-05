package com.esport.torneo.presentation.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
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

import com.esport.torneo.application.dto.TournamentCreateDto;
import com.esport.torneo.application.dto.TournamentDto;
import com.esport.torneo.application.dto.TournamentUpdateDto;
import com.esport.torneo.application.service.TournamentApplicationService;
import com.esport.torneo.application.service.TournamentApplicationService.TournamentStatsDto;
import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.infrastructure.config.RedisConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de torneos.
 * 
 * Proporciona endpoints para operaciones CRUD de torneos,
 * gestión de participantes, cambios de estado y consultas.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/tournaments")
@Tag(name = "Torneos", description = "API para gestión de torneos de e-sports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TournamentController {

    private static final Logger logger = LoggerFactory.getLogger(TournamentController.class);

    private final TournamentApplicationService tournamentApplicationService;

    /**
     * Constructor del controlador.
     * 
     * @param tournamentApplicationService servicio de aplicación de torneos
     */
    public TournamentController(TournamentApplicationService tournamentApplicationService) {
        this.tournamentApplicationService = tournamentApplicationService;
    }

    /**
     * Crea un nuevo torneo.
     * 
     * @param createDto datos para crear el torneo
     * @return el torneo creado
     */
    @PostMapping
    @Operation(summary = "Crear nuevo torneo", description = "Crea un nuevo torneo con los datos proporcionados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para crear torneos")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> createTournament(
            @Valid @RequestBody TournamentCreateDto createDto) {
        
        logger.info("Creando nuevo torneo: {}", createDto.getName());
        
        try {
            TournamentDto createdTournament = tournamentApplicationService.createTournament(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error creando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene un torneo por ID.
     * 
     * @param id ID del torneo
     * @return el torneo encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener torneo por ID", description = "Obtiene un torneo específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo encontrado"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TournamentDto> getTournamentById(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.debug("Obteniendo torneo por ID: {}", id);
        
        try {
            TournamentDto tournament = tournamentApplicationService.getTournamentById(id);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Torneo no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene información pública de un torneo.
     * 
     * @param id ID del torneo
     * @return información pública del torneo
     */
    @GetMapping("/{id}/public")
    @Operation(summary = "Obtener información pública del torneo", description = "Obtiene información pública de un torneo sin requerir autenticación")
    @ApiResponse(responseCode = "200", description = "Información pública obtenida")
    public ResponseEntity<TournamentDto> getPublicTournamentInfo(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.debug("Obteniendo información pública del torneo: {}", id);
        
        try {
            TournamentDto tournament = tournamentApplicationService.getPublicTournamentInfo(id);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Torneo no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todos los torneos con paginación.
     * 
     * @param pageable información de paginación
     * @return página de torneos
     */
    @GetMapping
    @Operation(summary = "Obtener todos los torneos", description = "Obtiene una lista paginada de todos los torneos")
    @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida exitosamente")
    @Cacheable(value = RedisConfig.TOURNAMENTS_CACHE, key = "'all_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ResponseEntity<Page<TournamentDto>> getAllTournaments(
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo todos los torneos");
        
        Page<TournamentDto> tournaments = tournamentApplicationService.getAllTournaments(pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Busca torneos por nombre.
     * 
     * @param name nombre a buscar
     * @param pageable información de paginación
     * @return página de torneos que coinciden con el nombre
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar torneos por nombre", description = "Busca torneos que contengan el nombre especificado")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    public ResponseEntity<Page<TournamentDto>> searchTournamentsByName(
            @Parameter(description = "Nombre a buscar") @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Buscando torneos por nombre: {}", name);
        
        Page<TournamentDto> tournaments = tournamentApplicationService.searchTournamentsByName(name, pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos por categoría.
     * 
     * @param categoryId ID de la categoría
     * @param pageable información de paginación
     * @return página de torneos de la categoría
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Obtener torneos por categoría", description = "Obtiene torneos filtrados por categoría")
    @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida exitosamente")
    public ResponseEntity<Page<TournamentDto>> getTournamentsByCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo torneos por categoría: {}", categoryId);
        
        Page<TournamentDto> tournaments = tournamentApplicationService.getTournamentsByCategory(categoryId, pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos por juego.
     * 
     * @param gameId ID del juego
     * @param pageable información de paginación
     * @return página de torneos del juego
     */
    @GetMapping("/game/{gameId}")
    @Operation(summary = "Obtener torneos por juego", description = "Obtiene torneos filtrados por juego")
    @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida exitosamente")
    public ResponseEntity<Page<TournamentDto>> getTournamentsByGame(
            @Parameter(description = "ID del juego") @PathVariable Long gameId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo torneos por juego: {}", gameId);
        
        Page<TournamentDto> tournaments = tournamentApplicationService.getTournamentsByGame(gameId, pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos por estado.
     * 
     * @param status estado de los torneos
     * @param pageable información de paginación
     * @return página de torneos con el estado especificado
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener torneos por estado", description = "Obtiene torneos filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida exitosamente")
    public ResponseEntity<Page<TournamentDto>> getTournamentsByStatus(
            @Parameter(description = "Estado del torneo") @PathVariable TournamentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo torneos por estado: {}", status);
        
        Page<TournamentDto> tournaments = tournamentApplicationService.getTournamentsByStatus(status, pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos próximos a iniciar.
     * 
     * @param hours horas hacia adelante para buscar
     * @param pageable información de paginación
     * @return página de torneos próximos
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Obtener torneos próximos", description = "Obtiene torneos que van a iniciar en las próximas horas")
    @ApiResponse(responseCode = "200", description = "Lista de torneos próximos obtenida")
    public ResponseEntity<Page<TournamentDto>> getUpcomingTournaments(
            @Parameter(description = "Horas hacia adelante") @RequestParam(defaultValue = "24") int hours,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo torneos próximos en {} horas", hours);
        
        LocalDateTime endTime = LocalDateTime.now().plusHours(hours);
        Page<TournamentDto> tournaments = tournamentApplicationService.getUpcomingTournaments(endTime, pageable);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Actualiza un torneo.
     * 
     * @param id ID del torneo
     * @param updateDto datos para actualizar
     * @return el torneo actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar torneo", description = "Actualiza los datos de un torneo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para actualizar este torneo")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> updateTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id,
            @Valid @RequestBody TournamentUpdateDto updateDto) {
        
        logger.info("Actualizando torneo: {}", id);
        
        try {
            TournamentDto updatedTournament = tournamentApplicationService.updateTournament(id, updateDto);
            return ResponseEntity.ok(updatedTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error actualizando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Publica un torneo (cambia estado a PUBLISHED).
     * 
     * @param id ID del torneo
     * @return el torneo publicado
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "Publicar torneo", description = "Cambia el estado del torneo a PUBLISHED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo publicado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede publicar el torneo"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> publishTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.info("Publicando torneo: {}", id);
        
        try {
            TournamentDto publishedTournament = tournamentApplicationService.publishTournament(id);
            return ResponseEntity.ok(publishedTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error publicando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Inicia un torneo (cambia estado a IN_PROGRESS).
     * 
     * @param id ID del torneo
     * @return el torneo iniciado
     */
    @PutMapping("/{id}/start")
    @Operation(summary = "Iniciar torneo", description = "Cambia el estado del torneo a IN_PROGRESS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo iniciado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede iniciar el torneo"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> startTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.info("Iniciando torneo: {}", id);
        
        try {
            TournamentDto startedTournament = tournamentApplicationService.startTournament(id);
            return ResponseEntity.ok(startedTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error iniciando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Finaliza un torneo (cambia estado a FINISHED).
     * 
     * @param id ID del torneo
     * @return el torneo finalizado
     */
    @PutMapping("/{id}/finish")
    @Operation(summary = "Finalizar torneo", description = "Cambia el estado del torneo a FINISHED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo finalizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede finalizar el torneo"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> finishTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.info("Finalizando torneo: {}", id);
        
        try {
            TournamentDto finishedTournament = tournamentApplicationService.finishTournament(id);
            return ResponseEntity.ok(finishedTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error finalizando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancela un torneo (cambia estado a CANCELLED).
     * 
     * @param id ID del torneo
     * @return el torneo cancelado
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancelar torneo", description = "Cambia el estado del torneo a CANCELLED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo cancelado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede cancelar el torneo"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> cancelTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.info("Cancelando torneo: {}", id);
        
        try {
            TournamentDto cancelledTournament = tournamentApplicationService.cancelTournament(id);
            return ResponseEntity.ok(cancelledTournament);
        } catch (IllegalArgumentException e) {
            logger.warn("Error cancelando torneo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Registra un participante en un torneo.
     * 
     * @param tournamentId ID del torneo
     * @param userId ID del usuario
     * @param teamName nombre del equipo (opcional)
     * @return confirmación del registro
     */
    @PostMapping("/{tournamentId}/participants")
    @Operation(summary = "Registrar participante", description = "Registra un usuario como participante en el torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Participante registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede registrar el participante"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> registerParticipant(
            @Parameter(description = "ID del torneo") @PathVariable Long tournamentId,
            @Parameter(description = "ID del usuario") @RequestParam Long userId,
            @Parameter(description = "Nombre del equipo") @RequestParam(required = false) String teamName) {
        
        logger.info("Registrando participante {} en torneo {}", userId, tournamentId);
        
        try {
            tournamentApplicationService.registerParticipant(tournamentId, userId, teamName);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error registrando participante: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina un torneo (soft delete).
     * 
     * @param id ID del torneo
     * @return confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar torneo", description = "Elimina un torneo (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Torneo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar este torneo")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        logger.info("Eliminando torneo: {}", id);
        
        try {
            tournamentApplicationService.deleteTournament(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error eliminando torneo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene estadísticas de torneos.
     * 
     * @return estadísticas generales de torneos
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de torneos", description = "Obtiene estadísticas generales sobre los torneos del sistema")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentStatsDto> getTournamentStats() {
        logger.debug("Obteniendo estadísticas de torneos");
        
        TournamentStatsDto stats = tournamentApplicationService.getTournamentStats();
        return ResponseEntity.ok(stats);
    }
} 