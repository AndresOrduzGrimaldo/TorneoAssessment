package com.esport.torneo.infrastructure.repository;

import com.esport.torneo.domain.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ticket.
 * 
 * Proporciona operaciones CRUD y consultas personalizadas
 * para gestionar los tickets de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca un ticket por su código único.
     * 
     * @param ticketCode el código del ticket
     * @return el ticket si existe
     */
    Optional<Ticket> findByTicketCode(String ticketCode);

    /**
     * Busca tickets activos por código.
     * 
     * @param ticketCode el código del ticket
     * @return el ticket si existe y está activo
     */
    Optional<Ticket> findByTicketCodeAndActiveTrue(String ticketCode);

    /**
     * Busca tickets por usuario.
     * 
     * @param userId el ID del usuario
     * @return lista de tickets del usuario
     */
    List<Ticket> findByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    /**
     * Busca tickets por torneo.
     * 
     * @param tournamentId el ID del torneo
     * @return lista de tickets del torneo
     */
    List<Ticket> findByTournamentIdAndActiveTrueOrderByCreatedAtDesc(Long tournamentId);

    /**
     * Busca tickets por estado.
     * 
     * @param status el estado del ticket
     * @return lista de tickets en el estado especificado
     */
    List<Ticket> findByStatusAndActiveTrueOrderByCreatedAtDesc(Ticket.TicketStatus status);

    /**
     * Busca tickets por usuario y torneo.
     * 
     * @param userId el ID del usuario
     * @param tournamentId el ID del torneo
     * @return lista de tickets del usuario para el torneo
     */
    List<Ticket> findByUserIdAndTournamentIdAndActiveTrue(Long userId, Long tournamentId);

    /**
     * Busca tickets válidos para uso (pagados y no expirados).
     * 
     * @param now fecha y hora actual
     * @return lista de tickets válidos
     */
    @Query("SELECT t FROM Ticket t WHERE t.active = true AND t.status = 'PAID' AND " +
           "t.expirationDate > :now ORDER BY t.expirationDate")
    List<Ticket> findValidTickets(@Param("now") LocalDateTime now);

    /**
     * Busca tickets expirados que aún no han sido marcados como tal.
     * 
     * @param now fecha y hora actual
     * @return lista de tickets expirados
     */
    @Query("SELECT t FROM Ticket t WHERE t.active = true AND " +
           "t.expirationDate <= :now AND t.status NOT IN ('EXPIRED', 'USED', 'CANCELLED') " +
           "ORDER BY t.expirationDate")
    List<Ticket> findExpiredTickets(@Param("now") LocalDateTime now);

    /**
     * Busca tickets próximos a expirar.
     * 
     * @param deadline fecha límite de expiración
     * @return lista de tickets próximos a expirar
     */
    @Query("SELECT t FROM Ticket t WHERE t.active = true AND t.status = 'PAID' AND " +
           "t.expirationDate <= :deadline AND t.expirationDate > CURRENT_TIMESTAMP " +
           "ORDER BY t.expirationDate")
    List<Ticket> findTicketsExpiringBy(@Param("deadline") LocalDateTime deadline);

    /**
     * Busca tickets reservados sin pagar hace tiempo.
     * 
     * @param deadline fecha límite para reservas
     * @return lista de tickets reservados vencidos
     */
    @Query("SELECT t FROM Ticket t WHERE t.active = true AND t.status = 'RESERVED' AND " +
           "t.createdAt <= :deadline ORDER BY t.createdAt")
    List<Ticket> findExpiredReservations(@Param("deadline") LocalDateTime deadline);

    /**
     * Cuenta tickets por estado y torneo.
     * 
     * @param tournamentId el ID del torneo
     * @param status el estado del ticket
     * @return número de tickets
     */
    long countByTournamentIdAndStatusAndActiveTrue(Long tournamentId, Ticket.TicketStatus status);

    /**
     * Cuenta tickets por usuario y estado.
     * 
     * @param userId el ID del usuario
     * @param status el estado del ticket
     * @return número de tickets
     */
    long countByUserIdAndStatusAndActiveTrue(Long userId, Ticket.TicketStatus status);

    /**
     * Verifica si existe un ticket con el código dado.
     * 
     * @param ticketCode el código a verificar
     * @return true si existe
     */
    boolean existsByTicketCode(String ticketCode);

    /**
     * Verifica si existe un ticket activo con el código dado.
     * 
     * @param ticketCode el código a verificar
     * @return true si existe y está activo
     */
    boolean existsByTicketCodeAndActiveTrue(String ticketCode);

    /**
     * Busca estadísticas de tickets por torneo.
     * 
     * @param tournamentId el ID del torneo
     * @return estadísticas del torneo
     */
    @Query("SELECT " +
           "COUNT(t) as total, " +
           "SUM(CASE WHEN t.status = 'RESERVED' THEN 1 ELSE 0 END) as reserved, " +
           "SUM(CASE WHEN t.status = 'PAID' THEN 1 ELSE 0 END) as paid, " +
           "SUM(CASE WHEN t.status = 'USED' THEN 1 ELSE 0 END) as used, " +
           "SUM(CASE WHEN t.status = 'EXPIRED' THEN 1 ELSE 0 END) as expired, " +
           "SUM(CASE WHEN t.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled, " +
           "SUM(CASE WHEN t.status = 'PAID' THEN t.price ELSE 0 END) as revenue, " +
           "SUM(CASE WHEN t.status = 'PAID' THEN t.commission ELSE 0 END) as commission " +
           "FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.active = true")
    Object[] findTicketStatsByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Busca ingresos totales por torneo.
     * 
     * @param tournamentId el ID del torneo
     * @return ingresos totales
     */
    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.tournamentId = :tournamentId AND " +
           "t.status = 'PAID' AND t.active = true")
    Optional<java.math.BigDecimal> findTotalRevenueByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Busca comisión total por torneo.
     * 
     * @param tournamentId el ID del torneo
     * @return comisión total
     */
    @Query("SELECT SUM(t.commission) FROM Ticket t WHERE t.tournamentId = :tournamentId AND " +
           "t.status = 'PAID' AND t.active = true")
    Optional<java.math.BigDecimal> findTotalCommissionByTournament(@Param("tournamentId") Long tournamentId);
} 