package com.esport.torneo.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.esport.torneo.application.dto.GameDto;
import com.esport.torneo.application.service.GameApplicationService;
import com.esport.torneo.application.service.GameApplicationService.GameStatsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de juegos.
 * 
 * Proporciona endpoints para operaciones CRUD y consultas
 * relacionadas con videojuegos disponibles en torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "API para gestión de videojuegos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameApplicationService gameApplicationService;

    /**
     * Constructor del controlador.
     * 
     * @param gameApplicationService servicio de aplicación de juegos
     */
    public GameController(GameApplicationService gameApplicationService) {
        this.gameApplicationService = gameApplicationService;
    }

    /**
     * Crea un nuevo juego.
     * 
     * @param gameDto datos del juego a crear
     * @return el juego creado
     */
    @PostMapping
    @Operation(summary = "Crear nuevo juego", description = "Crea un nuevo videojuego en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Juego creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe un juego con el mismo código")
    })
    public ResponseEntity<GameDto> createGame(
            @Valid @RequestBody GameDto gameDto) {
        
        logger.info("Creando nuevo juego: {}", gameDto.getCode());
        
        try {
            GameDto createdGame = gameApplicationService.createGame(gameDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGame);
        } catch (IllegalArgumentException e) {
            logger.warn("Error creando juego: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene un juego por ID.
     * 
     * @param id ID del juego
     * @return el juego encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener juego por ID", description = "Obtiene un juego específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Juego encontrado"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public ResponseEntity<GameDto> getGameById(
            @Parameter(description = "ID del juego") @PathVariable Long id) {
        
        logger.debug("Obteniendo juego por ID: {}", id);
        
        try {
            GameDto game = gameApplicationService.getGameById(id);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            logger.warn("Juego no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene un juego por código.
     * 
     * @param code código del juego
     * @return el juego encontrado
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener juego por código", description = "Obtiene un juego específico por su código único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Juego encontrado"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public ResponseEntity<GameDto> getGameByCode(
            @Parameter(description = "Código del juego") @PathVariable String code) {
        
        logger.debug("Obteniendo juego por código: {}", code);
        
        try {
            GameDto game = gameApplicationService.getGameByCode(code);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            logger.warn("Juego no encontrado: {}", code);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todos los juegos activos.
     * 
     * @return lista de juegos activos
     */
    @GetMapping
    @Operation(summary = "Listar juegos activos", description = "Obtiene todos los juegos activos del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de juegos obtenida exitosamente")
    public ResponseEntity<List<GameDto>> getAllActiveGames() {
        logger.debug("Obteniendo todos los juegos activos");
        
        List<GameDto> games = gameApplicationService.getAllActiveGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Obtiene juegos activos paginados.
     * 
     * @param pageable información de paginación
     * @return página de juegos
     */
    @GetMapping("/paged")
    @Operation(summary = "Listar juegos paginados", description = "Obtiene juegos activos con paginación")
    @ApiResponse(responseCode = "200", description = "Página de juegos obtenida exitosamente")
    public ResponseEntity<Page<GameDto>> getActiveGames(
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Obteniendo juegos paginados: página {}, tamaño {}", 
                     pageable.getPageNumber(), pageable.getPageSize());
        
        Page<GameDto> games = gameApplicationService.getActiveGames(pageable);
        return ResponseEntity.ok(games);
    }

    /**
     * Busca juegos por nombre.
     * 
     * @param name nombre a buscar
     * @return lista de juegos que coinciden
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar juegos por nombre", description = "Busca juegos que contengan el texto especificado en el nombre")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    public ResponseEntity<List<GameDto>> searchGamesByName(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String name) {
        
        logger.debug("Buscando juegos por nombre: {}", name);
        
        List<GameDto> games = gameApplicationService.searchGamesByName(name);
        return ResponseEntity.ok(games);
    }

    /**
     * Obtiene juegos por género.
     * 
     * @param genre género del juego
     * @return lista de juegos del género
     */
    @GetMapping("/genre/{genre}")
    @Operation(summary = "Obtener juegos por género", description = "Obtiene todos los juegos de un género específico")
    @ApiResponse(responseCode = "200", description = "Lista de juegos obtenida exitosamente")
    public ResponseEntity<List<GameDto>> getGamesByGenre(
            @Parameter(description = "Género del juego") @PathVariable String genre) {
        
        logger.debug("Obteniendo juegos por género: {}", genre);
        
        List<GameDto> games = gameApplicationService.getGamesByGenre(genre);
        return ResponseEntity.ok(games);
    }

    /**
     * Obtiene juegos por plataforma.
     * 
     * @param platform plataforma del juego
     * @return lista de juegos de la plataforma
     */
    @GetMapping("/platform/{platform}")
    @Operation(summary = "Obtener juegos por plataforma", description = "Obtiene todos los juegos de una plataforma específica")
    @ApiResponse(responseCode = "200", description = "Lista de juegos obtenida exitosamente")
    public ResponseEntity<List<GameDto>> getGamesByPlatform(
            @Parameter(description = "Plataforma del juego") @PathVariable String platform) {
        
        logger.debug("Obteniendo juegos por plataforma: {}", platform);
        
        List<GameDto> games = gameApplicationService.getGamesByPlatform(platform);
        return ResponseEntity.ok(games);
    }

    /**
     * Obtiene juegos para equipos.
     * 
     * @return lista de juegos que requieren equipos
     */
    @GetMapping("/team-games")
    @Operation(summary = "Obtener juegos de equipo", description = "Obtiene todos los juegos que requieren más de un jugador")
    @ApiResponse(responseCode = "200", description = "Lista de juegos de equipo obtenida exitosamente")
    public ResponseEntity<List<GameDto>> getTeamGames() {
        logger.debug("Obteniendo juegos para equipos");
        
        List<GameDto> games = gameApplicationService.getTeamGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Actualiza un juego existente.
     * 
     * @param id ID del juego a actualizar
     * @param gameDto datos actualizados
     * @return el juego actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar juego", description = "Actualiza un juego existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Juego actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con código existente")
    })
    public ResponseEntity<GameDto> updateGame(
            @Parameter(description = "ID del juego") @PathVariable Long id,
            @Valid @RequestBody GameDto gameDto) {
        
        logger.info("Actualizando juego: {}", id);
        
        try {
            GameDto updatedGame = gameApplicationService.updateGame(id, gameDto);
            return ResponseEntity.ok(updatedGame);
        } catch (IllegalArgumentException e) {
            logger.warn("Error actualizando juego: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un juego (soft delete).
     * 
     * @param id ID del juego a eliminar
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar juego", description = "Elimina un juego del sistema (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Juego eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar el juego")
    })
    public ResponseEntity<Void> deleteGame(
            @Parameter(description = "ID del juego") @PathVariable Long id) {
        
        logger.info("Eliminando juego: {}", id);
        
        try {
            gameApplicationService.deleteGame(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Juego no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar el juego: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Reactiva un juego eliminado.
     * 
     * @param id ID del juego a reactivar
     * @return el juego reactivado
     */
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivar juego", description = "Reactiva un juego previamente eliminado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Juego reactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
        @ApiResponse(responseCode = "409", description = "El juego ya está activo")
    })
    public ResponseEntity<GameDto> reactivateGame(
            @Parameter(description = "ID del juego") @PathVariable Long id) {
        
        logger.info("Reactivando juego: {}", id);
        
        try {
            GameDto reactivatedGame = gameApplicationService.reactivateGame(id);
            return ResponseEntity.ok(reactivatedGame);
        } catch (IllegalArgumentException e) {
            logger.warn("Juego no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("El juego ya está activo: {}", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Obtiene estadísticas de juegos.
     * 
     * @return estadísticas generales
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de juegos", description = "Obtiene estadísticas generales sobre los juegos del sistema")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<GameStatsDto> getGameStats() {
        logger.debug("Obteniendo estadísticas de juegos");
        
        GameStatsDto stats = gameApplicationService.getGameStats();
        return ResponseEntity.ok(stats);
    }
} 