package com.esport.torneo.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import com.esport.torneo.application.dto.GameDto;
import com.esport.torneo.application.service.GameApplicationService;
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

/**
 * REST Controller for Game management.
 * Provides endpoints for CRUD operations on games.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "Game management APIs")
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
     * Get all games with pagination.
     */
    @GetMapping
    @Operation(
        summary = "Get all games",
        description = "Retrieve a paginated list of all active games"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Games retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'all_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ResponseEntity<Page<GameDto>> getAllGames(
            @PageableDefault(size = 20) Pageable pageable) {
        List<GameDto> games = gameApplicationService.getAllActiveGames();
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), games.size());
        Page<GameDto> gamesPage = new PageImpl<>(
            games.subList(start, end), 
            pageable, 
            games.size()
        );
        
        return ResponseEntity.ok(gamesPage);
    }

    /**
     * Get all games as a simple list (for dropdowns).
     */
    @GetMapping("/list")
    @Operation(
        summary = "Get games list",
        description = "Retrieve a simple list of all active games for dropdown/select components"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Games list retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'all_list'")
    public ResponseEntity<List<GameDto>> getGamesList() {
        List<GameDto> games = gameApplicationService.getAllActiveGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Get game by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get game by ID",
        description = "Retrieve a specific game by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game found",
                    content = @Content(schema = @Schema(implementation = GameDto.class))),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'by_id_' + #id")
    public ResponseEntity<GameDto> getGameById(
            @Parameter(description = "Game ID", required = true)
            @PathVariable Long id) {
        try {
            GameDto game = gameApplicationService.getGameById(id);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get game by code.
     */
    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get game by code",
        description = "Retrieve a specific game by its unique code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game found",
                    content = @Content(schema = @Schema(implementation = GameDto.class))),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'by_code_' + #code")
    public ResponseEntity<GameDto> getGameByCode(
            @Parameter(description = "Game code", required = true)
            @PathVariable String code) {
        try {
            GameDto game = gameApplicationService.getGameByCode(code);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search games by name.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search games",
        description = "Search games by name (case insensitive, partial match)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<GameDto>> searchGames(
            @Parameter(description = "Search term for game name")
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<GameDto> games = gameApplicationService.searchGamesByName(name);
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), games.size());
        Page<GameDto> gamesPage = new PageImpl<>(
            games.subList(start, end), 
            pageable, 
            games.size()
        );
        
        return ResponseEntity.ok(gamesPage);
    }

    /**
     * Get games by genre.
     */
    @GetMapping("/genre/{genre}")
    @Operation(
        summary = "Get games by genre",
        description = "Retrieve games filtered by genre"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid genre"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<GameDto>> getGamesByGenre(
            @Parameter(description = "Game genre", required = true)
            @PathVariable String genre,
            @PageableDefault(size = 20) Pageable pageable) {
        
        List<GameDto> games = gameApplicationService.getGamesByGenre(genre);
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), games.size());
        Page<GameDto> gamesPage = new PageImpl<>(
            games.subList(start, end), 
            pageable, 
            games.size()
        );
        
        return ResponseEntity.ok(gamesPage);
    }

    /**
     * Get games by platform.
     */
    @GetMapping("/platform/{platform}")
    @Operation(
        summary = "Get games by platform",
        description = "Retrieve games filtered by platform"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid platform"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<GameDto>> getGamesByPlatform(
            @Parameter(description = "Game platform", required = true)
            @PathVariable String platform,
            @PageableDefault(size = 20) Pageable pageable) {
        
        List<GameDto> games = gameApplicationService.getGamesByPlatform(platform);
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), games.size());
        Page<GameDto> gamesPage = new PageImpl<>(
            games.subList(start, end), 
            pageable, 
            games.size()
        );
        
        return ResponseEntity.ok(gamesPage);
    }

    /**
     * Get available genres.
     */
    @GetMapping("/genres")
    @Operation(
        summary = "Get available genres",
        description = "Retrieve list of all available game genres"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Genres retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'genres'")
    public ResponseEntity<List<String>> getAvailableGenres() {
        Object stats = gameApplicationService.getGameStats();
        if (stats instanceof GameApplicationService.GameStatsDto gameStatsDto) {
            return ResponseEntity.ok(gameStatsDto.getGenres());
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get available platforms.
     */
    @GetMapping("/platforms")
    @Operation(
        summary = "Get available platforms",
        description = "Retrieve list of all available game platforms"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Platforms retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.GAMES_CACHE, key = "'platforms'")
    public ResponseEntity<List<String>> getAvailablePlatforms() {
        Object stats = gameApplicationService.getGameStats();
        if (stats instanceof GameApplicationService.GameStatsDto gameStatsDto) {
            return ResponseEntity.ok(gameStatsDto.getPlatforms());
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Create a new game.
     */
    @PostMapping
    @Operation(
        summary = "Create game",
        description = "Create a new game. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Game created successfully",
                    content = @Content(schema = @Schema(implementation = GameDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "409", description = "Game code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.GAMES_CACHE, allEntries = true)
    public ResponseEntity<GameDto> createGame(
            @Parameter(description = "Game data", required = true)
            @Valid @RequestBody GameDto request) {
        try {
            GameDto game = gameApplicationService.createGame(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Update an existing game.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update game",
        description = "Update an existing game. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game updated successfully",
                    content = @Content(schema = @Schema(implementation = GameDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "409", description = "Game code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.GAMES_CACHE, allEntries = true)
    public ResponseEntity<GameDto> updateGame(
            @Parameter(description = "Game ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated game data", required = true)
            @Valid @RequestBody GameDto request) {
        try {
            GameDto game = gameApplicationService.updateGame(id, request);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a game (soft delete).
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete game",
        description = "Soft delete a game. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Game deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "409", description = "Game is being used by tournaments"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.GAMES_CACHE, allEntries = true)
    public ResponseEntity<Void> deleteGame(
            @Parameter(description = "Game ID", required = true)
            @PathVariable Long id) {
        try {
            gameApplicationService.deleteGame(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get game statistics.
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get game statistics",
        description = "Get statistics about games usage. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getGameStats() {
        Object stats = gameApplicationService.getGameStats();
        return ResponseEntity.ok(stats);
    }
} 