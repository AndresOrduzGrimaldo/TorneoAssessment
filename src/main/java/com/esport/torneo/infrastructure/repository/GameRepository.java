package com.esport.torneo.infrastructure.repository;

import com.esport.torneo.domain.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
} 