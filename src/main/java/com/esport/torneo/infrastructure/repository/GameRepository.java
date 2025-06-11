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
     * Busca un juego por nombre (ignorando mayúsculas/minúsculas).
     *
     * @param name el nombre a buscar
     * @return el juego encontrado o empty
     */
    Optional<Game> findByNameIgnoreCase(String name);

    /**
     * Busca juegos activos.
     *
     * @return lista de juegos activos
     */
    List<Game> findByActiveTrue();

    /**
     * Busca juegos activos con paginación.
     *
     * @param pageable información de paginación
     * @return página de juegos activos
     */
    Page<Game> findByActiveTrue(Pageable pageable);

    /**
     * Busca juegos por categoría.
     *
     * @param categoryId ID de la categoría
     * @param pageable información de paginación
     * @return página de juegos de la categoría
     */
    @Query("SELECT g FROM Game g WHERE g.category.id = :categoryId")
    Page<Game> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Busca juegos activos por categoría.
     *
     * @param categoryId ID de la categoría
     * @param pageable información de paginación
     * @return página de juegos activos de la categoría
     */
    @Query("SELECT g FROM Game g WHERE g.category.id = :categoryId AND g.active = true")
    Page<Game> findActiveByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Busca juegos por nombre que contenga el texto (ignorando mayúsculas).
     *
     * @param name el texto a buscar en el nombre
     * @param pageable información de paginación
     * @return página de juegos que coinciden
     */
    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Busca juegos activos por nombre que contenga el texto.
     *
     * @param name el texto a buscar
     * @param pageable información de paginación
     * @return página de juegos activos que coinciden
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Game> findActiveByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Busca juegos por género.
     *
     * @param genre el género a buscar
     * @param pageable información de paginación
     * @return página de juegos del género
     */
    Page<Game> findByGenreIgnoreCase(String genre, Pageable pageable);

    /**
     * Busca juegos por plataforma.
     *
     * @param platform la plataforma a buscar
     * @param pageable información de paginación
     * @return página de juegos de la plataforma
     */
    Page<Game> findByPlatformContainingIgnoreCase(String platform, Pageable pageable);

    /**
     * Busca juegos por rango de jugadores.
     *
     * @param players número de jugadores
     * @param pageable información de paginación
     * @return página de juegos que soportan ese número de jugadores
     */
    @Query("SELECT g FROM Game g WHERE g.minPlayers <= :players AND g.maxPlayers >= :players")
    Page<Game> findByPlayerCount(@Param("players") Integer players, Pageable pageable);

    /**
     * Busca juegos activos por rango de jugadores.
     *
     * @param players número de jugadores
     * @param pageable información de paginación
     * @return página de juegos activos que soportan ese número de jugadores
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND g.minPlayers <= :players AND g.maxPlayers >= :players")
    Page<Game> findActiveByPlayerCount(@Param("players") Integer players, Pageable pageable);

    /**
     * Verifica si existe un juego con el nombre dado (ignorando mayúsculas).
     *
     * @param name el nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica si existe otro juego con el mismo nombre (para updates).
     *
     * @param name el nombre a verificar
     * @param id el ID del juego a excluir
     * @return true si existe otro juego con ese nombre
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Cuenta los juegos activos.
     *
     * @return número de juegos activos
     */
    long countByActiveTrue();

    /**
     * Cuenta los juegos por categoría.
     *
     * @param categoryId ID de la categoría
     * @return número de juegos en la categoría
     */
    @Query("SELECT COUNT(g) FROM Game g WHERE g.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Busca juegos sin borrado lógico.
     *
     * @return lista de juegos no eliminados
     */
    @Query("SELECT g FROM Game g WHERE g.deletedAt IS NULL")
    List<Game> findAllNotDeleted();

    /**
     * Busca juegos activos sin borrado lógico.
     *
     * @return lista de juegos activos no eliminados
     */
    @Query("SELECT g FROM Game g WHERE g.active = true AND g.deletedAt IS NULL")
    List<Game> findActiveNotDeleted();

    /**
     * Busca juegos populares (por número de torneos).
     *
     * @param pageable información de paginación
     * @return página de juegos ordenados por popularidad
     */
    @Query("SELECT g FROM Game g LEFT JOIN Tournament t ON t.game = g " +
           "WHERE g.active = true AND g.deletedAt IS NULL " +
           "GROUP BY g ORDER BY COUNT(t) DESC")
    Page<Game> findPopularGames(Pageable pageable);
} 