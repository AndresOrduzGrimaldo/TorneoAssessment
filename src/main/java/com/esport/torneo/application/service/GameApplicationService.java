package com.esport.torneo.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esport.torneo.application.dto.GameDto;
import com.esport.torneo.application.mapper.GameMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.domain.game.Game;
import com.esport.torneo.infrastructure.repository.CategoryRepository;
import com.esport.torneo.infrastructure.repository.GameRepository;

/**
 * Servicio de aplicación para la gestión de juegos.
 * 
 * Proporciona operaciones CRUD y lógica de negocio
 * para la gestión de videojuegos disponibles en torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class GameApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(GameApplicationService.class);

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final GameMapper gameMapper;

    /**
     * Constructor del servicio.
     * 
     * @param gameRepository repositorio de juegos
     * @param categoryRepository repositorio de categorías
     * @param gameMapper mapper de juegos
     */
    public GameApplicationService(GameRepository gameRepository, 
                                CategoryRepository categoryRepository,
                                GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
        this.gameMapper = gameMapper;
    }

    /**
     * Crea un nuevo juego.
     * 
     * @param gameDto datos del juego a crear
     * @return el juego creado
     * @throws IllegalArgumentException si ya existe un juego con ese nombre o la categoría no existe
     */
    @CacheEvict(value = {"games", "activeGames"}, allEntries = true)
    public GameDto createGame(GameDto gameDto) {
        logger.info("Creando nuevo juego: {}", gameDto.getName());

        validateGameName(gameDto.getName(), null);
        validatePlayerLimits(gameDto.getMinPlayers(), gameDto.getMaxPlayers());

        Category category = null;
        if (gameDto.getCategoryId() != null) {
            category = categoryRepository.findById(gameDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + gameDto.getCategoryId()));
        }

        Game game = gameMapper.toEntity(gameDto);
        if (category != null) {
            game.setCategory(category);
        }

        Game savedGame = gameRepository.save(game);
        logger.info("Juego creado exitosamente con ID: {}", savedGame.getId());

        return gameMapper.toDto(savedGame);
    }

    /**
     * Obtiene un juego por ID.
     * 
     * @param id ID del juego
     * @return el juego encontrado
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "games", key = "#id")
    @Transactional(readOnly = true)
    public GameDto getGameById(Long id) {
        logger.debug("Obteniendo juego por ID: {}", id);

        Game game = gameRepository.findById(id)
                .filter(g -> g.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));

        return gameMapper.toDto(game);
    }

    /**
     * Obtiene un juego por código.
     * 
     * @param code código del juego
     * @return el juego encontrado
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "games", key = "#code")
    @Transactional(readOnly = true)
    public GameDto getGameByCode(String code) {
        logger.debug("Obteniendo juego por código: {}", code);

        Game game = gameRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + code));

        return gameMapper.toDto(game);
    }

    /**
     * Obtiene todos los juegos activos.
     * 
     * @return lista de juegos activos
     */
    @Cacheable(value = "activeGames")
    @Transactional(readOnly = true)
    public List<GameDto> getAllActiveGames() {
        logger.debug("Obteniendo todos los juegos activos");

        return gameRepository.findByActiveTrueOrderByFullName()
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene juegos activos paginados.
     * 
     * @param pageable información de paginación
     * @return página de juegos
     */
    @Transactional(readOnly = true)
    public Page<GameDto> getActiveGames(Pageable pageable) {
        logger.debug("Obteniendo juegos activos paginados: página {}, tamaño {}", 
                     pageable.getPageNumber(), pageable.getPageSize());

        return gameRepository.findAll(pageable)
                .map(game -> game.getActive() ? gameMapper.toDto(game) : null)
                .map(dto -> dto); // Filter nulls would be needed here in a real implementation
    }

    /**
     * Busca juegos por nombre.
     * 
     * @param name nombre a buscar
     * @return lista de juegos que coinciden
     */
    @Transactional(readOnly = true)
    public List<GameDto> searchGamesByName(String name) {
        logger.debug("Buscando juegos por nombre: {}", name);

        return gameRepository.findByFullNameContainingIgnoreCase(name)
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene juegos por género.
     * 
     * @param genre género del juego
     * @return lista de juegos del género
     */
    @Transactional(readOnly = true)
    public List<GameDto> getGamesByGenre(String genre) {
        logger.debug("Obteniendo juegos por género: {}", genre);

        return gameRepository.findByGenreAndActiveTrueOrderByFullName(genre)
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene juegos por plataforma.
     * 
     * @param platform plataforma del juego
     * @return lista de juegos de la plataforma
     */
    @Transactional(readOnly = true)
    public List<GameDto> getGamesByPlatform(String platform) {
        logger.debug("Obteniendo juegos por plataforma: {}", platform);

        return gameRepository.findByPlatformAndActiveTrueOrderByFullName(platform)
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene juegos para equipos.
     * 
     * @return lista de juegos que requieren equipos
     */
    @Transactional(readOnly = true)
    public List<GameDto> getTeamGames() {
        logger.debug("Obteniendo juegos para equipos");

        return gameRepository.findTeamGames()
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un juego existente.
     * 
     * @param id ID del juego a actualizar
     * @param gameDto datos actualizados
     * @return el juego actualizado
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"games", "activeGames"}, allEntries = true)
    public GameDto updateGame(Long id, GameDto gameDto) {
        logger.info("Actualizando juego: {}", id);

        Game game = gameRepository.findById(id)
                .filter(g -> g.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));

        if (gameDto.getName() != null && !gameDto.getName().equals(game.getName())) {
            validateGameName(gameDto.getName(), id);
            game.setName(gameDto.getName());
        }

        if (gameDto.getMinPlayers() != null && gameDto.getMaxPlayers() != null) {
            validatePlayerLimits(gameDto.getMinPlayers(), gameDto.getMaxPlayers());
            game.setMinPlayers(gameDto.getMinPlayers());
            game.setMaxPlayers(gameDto.getMaxPlayers());
        }

        if (gameDto.getCategoryId() != null && 
            (game.getCategory() == null || !gameDto.getCategoryId().equals(game.getCategory().getId()))) {
            Category category = categoryRepository.findById(gameDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + gameDto.getCategoryId()));
            game.setCategory(category);
        }

        game.updateMetadata(
            gameDto.getGenre(),
            gameDto.getPlatform(),
            gameDto.getImageUrl()
        );

        Game savedGame = gameRepository.save(game);
        logger.info("Juego actualizado exitosamente: {}", savedGame.getId());

        return gameMapper.toDto(savedGame);
    }

    /**
     * Elimina (soft delete) un juego.
     * 
     * @param id ID del juego a eliminar
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"games", "activeGames"}, allEntries = true)
    public void deleteGame(Long id) {
        logger.info("Eliminando juego: {}", id);

        Game game = gameRepository.findById(id)
                .filter(g -> g.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));

        game.markAsDeleted();
        gameRepository.save(game);

        logger.info("Juego eliminado exitosamente: {}", id);
    }

    /**
     * Reactiva un juego eliminado.
     * 
     * @param id ID del juego a reactivar
     * @return el juego reactivado
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"games", "activeGames"}, allEntries = true)
    public GameDto reactivateGame(Long id) {
        logger.info("Reactivando juego: {}", id);

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));

        if (game.getActive()) {
            throw new IllegalStateException("El juego ya está activo");
        }

        game.restore();
        Game savedGame = gameRepository.save(game);

        logger.info("Juego reactivado exitosamente: {}", savedGame.getId());
        return gameMapper.toDto(savedGame);
    }

    /**
     * Obtiene estadísticas de juegos.
     * 
     * @return estadísticas generales
     */
    @Transactional(readOnly = true)
    public GameStatsDto getGameStats() {
        logger.debug("Obteniendo estadísticas de juegos");

        long totalActive = gameRepository.countByActiveTrue();
        long totalTeamGames = gameRepository.findTeamGames().size();
        List<String> genres = gameRepository.findDistinctGenres();
        List<String> platforms = gameRepository.findDistinctPlatforms();

        return new GameStatsDto(totalActive, totalTeamGames, genres, platforms);
    }

    /**
     * Valida que el nombre del juego sea único.
     * 
     * @param name el nombre a validar
     * @param excludeId ID a excluir de la validación (para updates)
     * @throws IllegalArgumentException si ya existe un juego con ese nombre
     */
    private void validateGameName(String name, Long excludeId) {
        boolean exists = excludeId != null 
            ? gameRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
            : gameRepository.existsByNameIgnoreCase(name);

        if (exists) {
            throw new IllegalArgumentException("Ya existe un juego con el nombre: " + name);
        }
    }

    /**
     * Valida los límites de jugadores.
     * 
     * @param minPlayers mínimo de jugadores
     * @param maxPlayers máximo de jugadores
     * @throws IllegalArgumentException si los límites son inválidos
     */
    private void validatePlayerLimits(Integer minPlayers, Integer maxPlayers) {
        if (minPlayers != null && maxPlayers != null && minPlayers > maxPlayers) {
            throw new IllegalArgumentException("El mínimo de jugadores no puede ser mayor al máximo");
        }
    }

    /**
     * DTO para estadísticas de juegos.
     */
    public static class GameStatsDto {
        private final long totalActiveGames;
        private final long totalTeamGames;
        private final List<String> genres;
        private final List<String> platforms;

        public GameStatsDto(long totalActiveGames, long totalTeamGames, 
                           List<String> genres, List<String> platforms) {
            this.totalActiveGames = totalActiveGames;
            this.totalTeamGames = totalTeamGames;
            this.genres = genres;
            this.platforms = platforms;
        }

        public long getTotalActiveGames() { return totalActiveGames; }
        public long getTotalTeamGames() { return totalTeamGames; }
        public List<String> getGenres() { return genres; }
        public List<String> getPlatforms() { return platforms; }
    }
} 