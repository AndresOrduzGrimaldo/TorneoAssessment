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
 * Repository interface for Tournament entity operations.
 * Provides data access methods for tournament management.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Find tournament by name (case insensitive).
     * 
     * @param name the tournament name
     * @return Optional containing the tournament if found
     */
    @Query("SELECT t FROM Tournament t WHERE UPPER(t.name) = UPPER(:name) AND t.deletedAt IS NULL")
    Optional<Tournament> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find all active tournaments (not soft deleted).
     * 
     * @return List of active tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE t.deletedAt IS NULL ORDER BY t.startDate DESC")
    List<Tournament> findAllActive();

    /**
     * Find active tournaments with pagination.
     * 
     * @param pageable pagination information
     * @return Page of active tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE t.deletedAt IS NULL")
    Page<Tournament> findAllActive(Pageable pageable);

    /**
     * Find tournaments by status.
     * 
     * @param status the tournament status
     * @param pageable pagination information
     * @return Page of tournaments with specified status
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.deletedAt IS NULL")
    Page<Tournament> findByStatus(@Param("status") TournamentStatus status, Pageable pageable);

    /**
     * Find tournaments by type.
     * 
     * @param type the tournament type
     * @param pageable pagination information
     * @return Page of tournaments with specified type
     */
    @Query("SELECT t FROM Tournament t WHERE t.type = :type AND t.deletedAt IS NULL")
    Page<Tournament> findByType(@Param("type") TournamentType type, Pageable pageable);

    /**
     * Find tournaments by organizer ID.
     * 
     * @param organizerId the organizer user ID
     * @param pageable pagination information
     * @return Page of tournaments organized by the user
     */
    @Query("SELECT t FROM Tournament t WHERE t.organizerId = :organizerId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Tournament> findByOrganizerId(@Param("organizerId") Long organizerId, Pageable pageable);

    /**
     * Find tournaments by category ID.
     * 
     * @param categoryId the category ID
     * @param pageable pagination information
     * @return Page of tournaments in the category
     */
    @Query("SELECT t FROM Tournament t WHERE t.category.id = :categoryId AND t.deletedAt IS NULL")
    Page<Tournament> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Find tournaments by game ID.
     * 
     * @param gameId the game ID
     * @param pageable pagination information
     * @return Page of tournaments for the game
     */
    @Query("SELECT t FROM Tournament t WHERE t.game.id = :gameId AND t.deletedAt IS NULL")
    Page<Tournament> findByGameId(@Param("gameId") Long gameId, Pageable pageable);

    /**
     * Find upcoming tournaments (start date in the future).
     * 
     * @param currentDate current date and time
     * @param pageable pagination information
     * @return Page of upcoming tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE t.startDate > :currentDate AND t.status = 'PUBLISHED' AND t.deletedAt IS NULL ORDER BY t.startDate ASC")
    Page<Tournament> findUpcomingTournaments(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);

    /**
     * Find ongoing tournaments (currently in progress).
     * 
     * @param currentDate current date and time
     * @param pageable pagination information
     * @return Page of ongoing tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE t.startDate <= :currentDate AND t.endDate >= :currentDate AND t.status = 'IN_PROGRESS' AND t.deletedAt IS NULL")
    Page<Tournament> findOngoingTournaments(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);

    /**
     * Find tournaments within date range.
     * 
     * @param startDate start of date range
     * @param endDate end of date range
     * @param pageable pagination information
     * @return Page of tournaments within the date range
     */
    @Query("SELECT t FROM Tournament t WHERE t.startDate >= :startDate AND t.startDate <= :endDate AND t.deletedAt IS NULL ORDER BY t.startDate ASC")
    Page<Tournament> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Search tournaments by name containing text (case insensitive).
     * 
     * @param name text to search in name
     * @param pageable pagination information
     * @return Page of matching tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE UPPER(t.name) LIKE UPPER(CONCAT('%', :name, '%')) AND t.deletedAt IS NULL")
    Page<Tournament> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Count free tournaments created by organizer.
     * 
     * @param organizerId the organizer user ID
     * @return number of free tournaments created by the organizer
     */
    @Query("SELECT COUNT(t) FROM Tournament t WHERE t.organizerId = :organizerId AND t.type = 'FREE' AND t.deletedAt IS NULL")
    long countFreeTournamentsByOrganizer(@Param("organizerId") Long organizerId);

    /**
     * Count tournaments by status.
     * 
     * @param status the tournament status
     * @return number of tournaments with the status
     */
    @Query("SELECT COUNT(t) FROM Tournament t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TournamentStatus status);

    /**
     * Find tournaments that need to be started (published and start date passed).
     * 
     * @param currentDate current date and time
     * @return List of tournaments that should be started
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = 'PUBLISHED' AND t.startDate <= :currentDate AND t.deletedAt IS NULL")
    List<Tournament> findTournamentsToStart(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find tournaments that need to be finished (in progress and end date passed).
     * 
     * @param currentDate current date and time
     * @return List of tournaments that should be finished
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = 'IN_PROGRESS' AND t.endDate <= :currentDate AND t.deletedAt IS NULL")
    List<Tournament> findTournamentsToFinish(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Check if tournament name exists (excluding specific ID).
     * 
     * @param name the tournament name
     * @param excludeId ID to exclude from search
     * @return true if name exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Tournament t WHERE UPPER(t.name) = UPPER(:name) AND t.id != :excludeId AND t.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Check if tournament name exists.
     * 
     * @param name the tournament name
     * @return true if name exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Tournament t WHERE UPPER(t.name) = UPPER(:name) AND t.deletedAt IS NULL")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Find popular tournaments (high participant count).
     * 
     * @param minParticipants minimum number of participants
     * @param pageable pagination information
     * @return Page of popular tournaments
     */
    @Query("SELECT t FROM Tournament t WHERE t.currentParticipants >= :minParticipants AND t.status IN ('PUBLISHED', 'IN_PROGRESS') AND t.deletedAt IS NULL ORDER BY t.currentParticipants DESC")
    Page<Tournament> findPopularTournaments(@Param("minParticipants") Integer minParticipants, Pageable pageable);

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