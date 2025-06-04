package com.esport.torneo.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.domain.tournament.TournamentType;

/**
 * Repositorio para la entidad Tournament.
 * 
 * Proporciona operaciones CRUD y consultas personalizadas
 * para gestionar los torneos de videojuegos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Busca torneos activos ordenados por fecha de inicio.
     * 
     * @return lista de torneos activos
     */
    List<Tournament> findByActiveTrueOrderByStartDate();

    /**
     * Busca torneos por estado.
     * 
     * @param status el estado del torneo
     * @return lista de torneos con el estado especificado
     */
    List<Tournament> findByStatusAndActiveTrueOrderByStartDate(TournamentStatus status);

    /**
     * Busca torneos por tipo.
     * 
     * @param type el tipo de torneo
     * @return lista de torneos del tipo especificado
     */
    List<Tournament> findByTournamentTypeAndActiveTrueOrderByStartDate(TournamentType type);

    /**
     * Busca torneos por organizador.
     * 
     * @param organizerId el ID del organizador
     * @return lista de torneos del organizador
     */
    List<Tournament> findByOrganizerIdAndActiveTrueOrderByStartDateDesc(Long organizerId);

    /**
     * Busca torneos por categoría.
     * 
     * @param categoryId el ID de la categoría
     * @return lista de torneos de la categoría
     */
    List<Tournament> findByCategoryIdAndActiveTrueOrderByStartDate(Long categoryId);

    /**
     * Busca torneos por juego.
     * 
     * @param gameId el ID del juego
     * @return lista de torneos del juego
     */
    List<Tournament> findByGameIdAndActiveTrueOrderByStartDate(Long gameId);

    /**
     * Busca torneos con paginación.
     * 
     * @param pageable configuración de paginación
     * @return página de torneos activos
     */
    Page<Tournament> findByActiveTrue(Pageable pageable);

    /**
     * Busca torneos por estado con paginación.
     * 
     * @param status el estado del torneo
     * @param pageable configuración de paginación
     * @return página de torneos
     */
    Page<Tournament> findByStatusAndActiveTrue(TournamentStatus status, Pageable pageable);

    /**
     * Busca torneos publicados que están en período de registro.
     * 
     * @param now fecha y hora actual
     * @return lista de torneos disponibles para registro
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.status = 'PUBLISHED' AND " +
           "t.registrationStart <= :now AND t.registrationEnd > :now " +
           "ORDER BY t.registrationEnd")
    List<Tournament> findTournamentsOpenForRegistration(@Param("now") LocalDateTime now);

    /**
     * Busca torneos que deben iniciarse pronto.
     * 
     * @param from fecha y hora desde
     * @param to fecha y hora hasta
     * @return lista de torneos próximos a iniciar
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.status = 'PUBLISHED' AND " +
           "t.startDate BETWEEN :from AND :to " +
           "ORDER BY t.startDate")
    List<Tournament> findTournamentsStartingSoon(@Param("from") LocalDateTime from, 
                                                  @Param("to") LocalDateTime to);

    /**
     * Busca torneos por nombre (búsqueda parcial, case insensitive).
     * 
     * @param name texto a buscar en el nombre
     * @return lista de torneos que coinciden
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY t.startDate DESC")
    List<Tournament> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca torneos por rango de fechas.
     * 
     * @param startDate fecha de inicio del rango
     * @param endDate fecha de fin del rango
     * @return lista de torneos en el rango
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND " +
           "t.startDate >= :startDate AND t.endDate <= :endDate " +
           "ORDER BY t.startDate")
    List<Tournament> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Busca torneos con cupos disponibles.
     * 
     * @return lista de torneos con espacios
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.status = 'PUBLISHED' AND " +
           "t.currentParticipants < t.maxParticipants " +
           "ORDER BY t.registrationEnd")
    List<Tournament> findTournamentsWithAvailableSlots();

    /**
     * Busca torneos por rango de premio.
     * 
     * @param minPrize premio mínimo
     * @param maxPrize premio máximo
     * @return lista de torneos en el rango de premios
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND " +
           "t.prizePool BETWEEN :minPrize AND :maxPrize " +
           "ORDER BY t.prizePool DESC")
    List<Tournament> findByPrizePoolRange(@Param("minPrize") BigDecimal minPrize,
                                          @Param("maxPrize") BigDecimal maxPrize);

    /**
     * Busca torneos gratuitos.
     * 
     * @return lista de torneos gratuitos
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND " +
           "t.tournamentType = 'FREE' AND t.entryFee = 0 " +
           "ORDER BY t.startDate")
    List<Tournament> findFreeTournaments();

    /**
     * Cuenta torneos por estado.
     * 
     * @param status el estado del torneo
     * @return número de torneos en el estado
     */
    long countByStatusAndActiveTrue(TournamentStatus status);

    /**
     * Cuenta torneos por organizador.
     * 
     * @param organizerId el ID del organizador
     * @return número de torneos del organizador
     */
    long countByOrganizerIdAndActiveTrue(Long organizerId);

    /**
     * Busca estadísticas de participación por torneo.
     * 
     * @return lista de estadísticas
     */
    @Query("SELECT t.id, t.name, t.maxParticipants, t.currentParticipants, " +
           "ROUND((CAST(t.currentParticipants AS DOUBLE) / t.maxParticipants * 100), 2) as fillPercentage " +
           "FROM Tournament t WHERE t.active = true AND t.status = 'PUBLISHED' " +
           "ORDER BY fillPercentage DESC")
    List<Object[]> findParticipationStats();

    /**
     * Busca torneos que expiran registraciones pronto.
     * 
     * @param deadline fecha límite
     * @return lista de torneos con registraciones por expirar
     */
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.status = 'PUBLISHED' AND " +
           "t.registrationEnd <= :deadline AND t.registrationEnd > CURRENT_TIMESTAMP " +
           "ORDER BY t.registrationEnd")
    List<Tournament> findTournamentsWithRegistrationDeadlineSoon(@Param("deadline") LocalDateTime deadline);

    /**
     * Busca el último torneo creado por un organizador.
     * 
     * @param organizerId el ID del organizador
     * @return el último torneo creado
     */
    Optional<Tournament> findTopByOrganizerIdAndActiveTrueOrderByCreatedAtDesc(Long organizerId);

    /**
     * Busca torneos populares (más participantes).
     * 
     * @param limit número máximo de resultados
     * @return lista de torneos populares
     */
    @Query(value = "SELECT * FROM tournaments WHERE active = true AND status = 'PUBLISHED' " +
                   "ORDER BY current_participants DESC LIMIT :limit", nativeQuery = true)
    List<Tournament> findPopularTournaments(@Param("limit") int limit);

    /**
     * Cuenta todos los torneos activos.
     * 
     * @return número total de torneos activos
     */
    long countByActiveTrue();

    /**
     * Busca un torneo por ID si está activo.
     * 
     * @param id ID del torneo
     * @return el torneo si existe y está activo
     */
    Optional<Tournament> findByIdAndActiveTrue(Long id);

    /**
     * Busca torneos por nombre con paginación (búsqueda parcial, case insensitive).
     * 
     * @param name texto a buscar en el nombre
     * @param pageable configuración de paginación
     * @return página de torneos que coinciden
     */
    Page<Tournament> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    /**
     * Busca torneos por categoría con paginación.
     * 
     * @param categoryId el ID de la categoría
     * @param pageable configuración de paginación
     * @return página de torneos de la categoría
     */
    Page<Tournament> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Busca torneos por juego con paginación.
     * 
     * @param gameId el ID del juego
     * @param pageable configuración de paginación
     * @return página de torneos del juego
     */
    Page<Tournament> findByGameIdAndActiveTrue(Long gameId, Pageable pageable);

    /**
     * Busca torneos por rango de fechas de inicio, estado y con paginación.
     * 
     * @param startDate fecha de inicio del rango
     * @param endDate fecha de fin del rango
     * @param status estado del torneo
     * @param pageable configuración de paginación
     * @return página de torneos en el rango
     */
    Page<Tournament> findByStartDateBetweenAndStatusAndActiveTrue(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            TournamentStatus status, 
            Pageable pageable);
} 