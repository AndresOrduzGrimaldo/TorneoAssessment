package com.esport.torneo.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esport.torneo.domain.game.Game;
import com.esport.torneo.domain.game.GameGenre;
import com.esport.torneo.domain.game.GamePlatform;

/**
 * Repositorio para la entidad Game.
 * 
 * Proporciona operaciones CRUD y consultas personalizadas
 * para gestionar los videojuegos disponibles en torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Busca un juego por su código único.
     * 
     * @param code el código del juego
     * @return el juego si existe
     */
    Optional<Game> findByCode(String code);

    /**
     * Busca un juego activo por su código.
     * 
     * @param code el código del juego
     * @return el juego si existe y está activo
     */
    Optional<Game> findByCodeAndActiveTrue(String code);

    /**
     * Busca un juego por ID si está activo.
     * 
     * @param id ID del juego
     * @return el juego si existe y está activo
     */
    Optional<Game> findByIdAndActiveTrue(Long id);

    /**
     * Busca juegos activos ordenados por nombre.
     * 
     * @return lista de juegos activos
     */
    List<Game> findByActiveTrueOrderByFullName();

    /**
     * Busca juegos por género.
     * 
     * @param genre el género del juego
     * @return lista de juegos del género especificado
     */
    List<Game> findByGenreAndActiveTrueOrderByFullName(String genre);

    /**
     * Busca juegos por plataforma.
     * 
     * @param platform la plataforma del juego
     * @return lista de juegos de la plataforma especificada
     */
    List<Game> findByPlatformAndActiveTrueOrderByFullName(String platform);

    /**
     * Busca juegos por número de jugadores.
     * 
     * @param playerCount número de jugadores
     * @return lista de juegos con el número de jugadores especificado
     */
    List<Game> findByPlayerCountAndActiveTrueOrderByFullName(Integer playerCount);

    /**
     * Busca juegos por nombre parcial (case insensitive).
     * 
     * @param name texto a buscar en el nombre
     * @return lista de juegos que coinciden
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND " +
           "LOWER(g.fullName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY g.fullName")
    List<Game> findByFullNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca juegos por código o nombre (búsqueda general).
     * 
     * @param searchTerm término de búsqueda
     * @return lista de juegos que coinciden
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND " +
           "(LOWER(g.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY g.fullName")
    List<Game> searchByCodeOrName(@Param("searchTerm") String searchTerm);

    /**
     * Busca todos los géneros únicos de juegos activos.
     * 
     * @return lista de géneros únicos
     */
    @Query("SELECT DISTINCT g.genre FROM Game g WHERE g.active = true AND g.genre IS NOT NULL " +
           "ORDER BY g.genre")
    List<String> findDistinctGenres();

    /**
     * Busca todas las plataformas únicas de juegos activos.
     * 
     * @return lista de plataformas únicas
     */
    @Query("SELECT DISTINCT g.platform FROM Game g WHERE g.active = true AND g.platform IS NOT NULL " +
           "ORDER BY g.platform")
    List<String> findDistinctPlatforms();

    /**
     * Verifica si existe un juego con el código dado.
     * 
     * @param code el código a verificar
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Verifica si existe un juego activo con el código dado.
     * 
     * @param code el código a verificar
     * @return true si existe y está activo
     */
    boolean existsByCodeAndActiveTrue(String code);

    /**
     * Cuenta los juegos activos.
     * 
     * @return número de juegos activos
     */
    long countByActiveTrue();

    /**
     * Busca juegos de equipo (más de 1 jugador).
     * 
     * @return lista de juegos de equipo
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND g.playerCount > 1 " +
           "ORDER BY g.playerCount, g.fullName")
    List<Game> findTeamGames();

    /**
     * Busca juegos individuales (1 jugador).
     * 
     * @return lista de juegos individuales
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND g.playerCount = 1 " +
           "ORDER BY g.fullName")
    List<Game> findSinglePlayerGames();

    /**
     * Find game by name (case insensitive).
     * 
     * @param name the game name
     * @return Optional containing the game if found
     */
    @Query("SELECT g FROM Game g WHERE UPPER(g.name) = UPPER(:name) AND g.deletedAt IS NULL")
    Optional<Game> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find all active games (not soft deleted).
     * 
     * @return List of active games
     */
    @Query("SELECT g FROM Game g WHERE g.deletedAt IS NULL ORDER BY g.name")
    List<Game> findAllActive();

    /**
     * Find active games with pagination.
     * 
     * @param pageable pagination information
     * @return Page of active games
     */
    @Query("SELECT g FROM Game g WHERE g.deletedAt IS NULL")
    Page<Game> findAllActive(Pageable pageable);

    /**
     * Search games by name containing text (case insensitive).
     * 
     * @param name text to search in name
     * @param pageable pagination information
     * @return Page of matching games
     */
    @Query("SELECT g FROM Game g WHERE UPPER(g.name) LIKE UPPER(CONCAT('%', :name, '%')) AND g.deletedAt IS NULL")
    Page<Game> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find games by genre.
     * 
     * @param genre the game genre
     * @param pageable pagination information
     * @return Page of games with specified genre
     */
    @Query("SELECT g FROM Game g WHERE g.genre = :genre AND g.deletedAt IS NULL")
    Page<Game> findByGenre(@Param("genre") GameGenre genre, Pageable pageable);

    /**
     * Find games by platform.
     * 
     * @param platform the game platform
     * @param pageable pagination information
     * @return Page of games with specified platform
     */
    @Query("SELECT g FROM Game g WHERE g.platform = :platform AND g.deletedAt IS NULL")
    Page<Game> findByPlatform(@Param("platform") GamePlatform platform, Pageable pageable);

    /**
     * Find games by player count range.
     * 
     * @param minPlayers minimum number of players
     * @param maxPlayers maximum number of players
     * @param pageable pagination information
     * @return Page of games within player count range
     */
    @Query("SELECT g FROM Game g WHERE g.minPlayers >= :minPlayers AND g.maxPlayers <= :maxPlayers AND g.deletedAt IS NULL")
    Page<Game> findByPlayerCountRange(@Param("minPlayers") Integer minPlayers, @Param("maxPlayers") Integer maxPlayers, Pageable pageable);

    /**
     * Check if game name exists (excluding specific ID).
     * 
     * @param name the game name
     * @param excludeId ID to exclude from search
     * @return true if name exists
     */
    @Query("SELECT COUNT(g) > 0 FROM Game g WHERE UPPER(g.name) = UPPER(:name) AND g.id != :excludeId AND g.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Check if game name exists.
     * 
     * @param name the game name
     * @return true if name exists
     */
    @Query("SELECT COUNT(g) > 0 FROM Game g WHERE UPPER(g.name) = UPPER(:name) AND g.deletedAt IS NULL")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Count active games.
     * 
     * @return number of active games
     */
    @Query("SELECT COUNT(g) FROM Game g WHERE g.deletedAt IS NULL")
    long countActive();

    /**
     * Find games by genre list.
     * 
     * @param genres list of game genres
     * @return List of games with specified genres
     */
    @Query("SELECT g FROM Game g WHERE g.genre IN :genres AND g.deletedAt IS NULL ORDER BY g.name")
    List<Game> findByGenreIn(@Param("genres") List<GameGenre> genres);

    /**
     * Find games by platform list.
     * 
     * @param platforms list of game platforms
     * @return List of games with specified platforms
     */
    @Query("SELECT g FROM Game g WHERE g.platform IN :platforms AND g.deletedAt IS NULL ORDER BY g.name")
    List<Game> findByPlatformIn(@Param("platforms") List<GamePlatform> platforms);

    /**
     * Find games suitable for specific player count.
     * 
     * @param playerCount the number of players
     * @return List of games that support the player count
     */
    @Query("SELECT g FROM Game g WHERE g.minPlayers <= :playerCount AND g.maxPlayers >= :playerCount AND g.deletedAt IS NULL ORDER BY g.name")
    List<Game> findBySupportedPlayerCount(@Param("playerCount") Integer playerCount);
} 