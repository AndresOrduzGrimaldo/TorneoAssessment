package com.esport.torneo.infrastructure.repository;

import com.esport.torneo.domain.ticket.Ticket;
import com.esport.torneo.domain.ticket.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ticket entity operations.
 * Provides data access methods for ticket management.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Find ticket by QR code.
     * 
     * @param qrCode the QR code
     * @return Optional containing the ticket if found
     */
    @Query("SELECT t FROM Ticket t WHERE t.qrCode = :qrCode AND t.deletedAt IS NULL")
    Optional<Ticket> findByQrCode(@Param("qrCode") String qrCode);

    /**
     * Find ticket by unique code.
     * 
     * @param uniqueCode the unique code
     * @return Optional containing the ticket if found
     */
    @Query("SELECT t FROM Ticket t WHERE t.uniqueCode = :uniqueCode AND t.deletedAt IS NULL")
    Optional<Ticket> findByUniqueCode(@Param("uniqueCode") String uniqueCode);

    /**
     * Find all active tickets (not soft deleted).
     * 
     * @return List of active tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Ticket> findAllActive();

    /**
     * Find active tickets with pagination.
     * 
     * @param pageable pagination information
     * @return Page of active tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.deletedAt IS NULL")
    Page<Ticket> findAllActive(Pageable pageable);

    /**
     * Find tickets by tournament ID.
     * 
     * @param tournamentId the tournament ID
     * @param pageable pagination information
     * @return Page of tickets for the tournament
     */
    @Query("SELECT t FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Ticket> findByTournamentId(@Param("tournamentId") Long tournamentId, Pageable pageable);

    /**
     * Find tickets by user ID.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return Page of tickets owned by the user
     */
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Ticket> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find tickets by status.
     * 
     * @param status the ticket status
     * @param pageable pagination information
     * @return Page of tickets with specified status
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.deletedAt IS NULL")
    Page<Ticket> findByStatus(@Param("status") TicketStatus status, Pageable pageable);

    /**
     * Find tickets by tournament and user.
     * 
     * @param tournamentId the tournament ID
     * @param userId the user ID
     * @return List of tickets for the tournament and user
     */
    @Query("SELECT t FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.userId = :userId AND t.deletedAt IS NULL")
    List<Ticket> findByTournamentIdAndUserId(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    /**
     * Find expired tickets that haven't been processed.
     * 
     * @param currentDate current date and time
     * @return List of expired tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.expiresAt <= :currentDate AND t.status IN ('RESERVED', 'PAID') AND t.deletedAt IS NULL")
    List<Ticket> findExpiredTickets(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find tickets expiring soon (within specified hours).
     * 
     * @param expirationTime expiration threshold
     * @return List of tickets expiring soon
     */
    @Query("SELECT t FROM Ticket t WHERE t.expiresAt <= :expirationTime AND t.expiresAt > CURRENT_TIMESTAMP AND t.status = 'RESERVED' AND t.deletedAt IS NULL")
    List<Ticket> findTicketsExpiringSoon(@Param("expirationTime") LocalDateTime expirationTime);

    /**
     * Count tickets by tournament and status.
     * 
     * @param tournamentId the tournament ID
     * @param status the ticket status
     * @return number of tickets with the status for the tournament
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.status = :status AND t.deletedAt IS NULL")
    long countByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") TicketStatus status);

    /**
     * Count tickets by user and status.
     * 
     * @param userId the user ID
     * @param status the ticket status
     * @return number of tickets with the status for the user
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.userId = :userId AND t.status = :status AND t.deletedAt IS NULL")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketStatus status);

    /**
     * Calculate total revenue for tournament.
     * 
     * @param tournamentId the tournament ID
     * @return total revenue from paid tickets
     */
    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.status IN ('PAID', 'USED') AND t.deletedAt IS NULL")
    BigDecimal calculateTournamentRevenue(@Param("tournamentId") Long tournamentId);

    /**
     * Calculate total commission for tournament.
     * 
     * @param tournamentId the tournament ID
     * @return total commission from paid tickets
     */
    @Query("SELECT COALESCE(SUM(t.commissionAmount), 0) FROM Ticket t WHERE t.tournamentId = :tournamentId AND t.status IN ('PAID', 'USED') AND t.deletedAt IS NULL")
    BigDecimal calculateTournamentCommission(@Param("tournamentId") Long tournamentId);

    /**
     * Find tickets by price range.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return Page of tickets within price range
     */
    @Query("SELECT t FROM Ticket t WHERE t.price >= :minPrice AND t.price <= :maxPrice AND t.deletedAt IS NULL")
    Page<Ticket> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Check if QR code exists (excluding specific ID).
     * 
     * @param qrCode the QR code
     * @param excludeId ID to exclude from search
     * @return true if QR code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.qrCode = :qrCode AND t.id != :excludeId AND t.deletedAt IS NULL")
    boolean existsByQrCodeAndIdNot(@Param("qrCode") String qrCode, @Param("excludeId") Long excludeId);

    /**
     * Check if unique code exists (excluding specific ID).
     * 
     * @param uniqueCode the unique code
     * @param excludeId ID to exclude from search
     * @return true if unique code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.uniqueCode = :uniqueCode AND t.id != :excludeId AND t.deletedAt IS NULL")
    boolean existsByUniqueCodeAndIdNot(@Param("uniqueCode") String uniqueCode, @Param("excludeId") Long excludeId);

    /**
     * Check if QR code exists.
     * 
     * @param qrCode the QR code
     * @return true if QR code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.qrCode = :qrCode AND t.deletedAt IS NULL")
    boolean existsByQrCode(@Param("qrCode") String qrCode);

    /**
     * Check if unique code exists.
     * 
     * @param uniqueCode the unique code
     * @return true if unique code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.uniqueCode = :uniqueCode AND t.deletedAt IS NULL")
    boolean existsByUniqueCode(@Param("uniqueCode") String uniqueCode);

    /**
     * Find tickets created within date range.
     * 
     * @param startDate start of date range
     * @param endDate end of date range
     * @param pageable pagination information
     * @return Page of tickets created within the date range
     */
    @Query("SELECT t FROM Ticket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Ticket> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Count active tickets.
     * 
     * @return number of active tickets
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deletedAt IS NULL")
    long countActive();

    /**
     * Find user's free tickets for tournament.
     * 
     * @param userId the user ID
     * @param tournamentId the tournament ID
     * @return List of free tickets for the user and tournament
     */
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.tournamentId = :tournamentId AND t.price = 0 AND t.deletedAt IS NULL")
    List<Ticket> findFreeTicketsByUserAndTournament(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);
} 