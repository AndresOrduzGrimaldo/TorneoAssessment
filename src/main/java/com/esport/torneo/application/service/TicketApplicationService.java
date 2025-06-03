package com.esport.torneo.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esport.torneo.application.dto.TicketDto;
import com.esport.torneo.application.mapper.TicketMapper;
import com.esport.torneo.domain.ticket.QrCodeService;
import com.esport.torneo.domain.ticket.Ticket;
import com.esport.torneo.domain.ticket.Ticket.TicketStatus;
import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.infrastructure.repository.TicketRepository;
import com.esport.torneo.infrastructure.repository.TournamentRepository;

/**
 * Servicio de aplicación para la gestión de tickets.
 * 
 * Proporciona operaciones CRUD y lógica de negocio
 * para la gestión de tickets virtuales de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class TicketApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TicketApplicationService.class);

    private final TicketRepository ticketRepository;
    private final TournamentRepository tournamentRepository;
    private final TicketMapper ticketMapper;
    private final QrCodeService qrCodeService;

    /**
     * Constructor del servicio.
     * 
     * @param ticketRepository repositorio de tickets
     * @param tournamentRepository repositorio de torneos
     * @param ticketMapper mapper de tickets
     * @param qrCodeService servicio de códigos QR
     */
    public TicketApplicationService(TicketRepository ticketRepository,
                                   TournamentRepository tournamentRepository,
                                   TicketMapper ticketMapper,
                                   QrCodeService qrCodeService) {
        this.ticketRepository = ticketRepository;
        this.tournamentRepository = tournamentRepository;
        this.ticketMapper = ticketMapper;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Crea un nuevo ticket para un torneo.
     * 
     * @param tournamentId ID del torneo
     * @param userId ID del usuario
     * @param price precio del ticket
     * @return el ticket creado
     * @throws IllegalArgumentException si el torneo no existe o no acepta tickets
     */
    @CacheEvict(value = {"tickets", "userTickets"}, allEntries = true)
    public TicketDto createTicket(Long tournamentId, Long userId, BigDecimal price) {
        logger.info("Creando ticket para torneo {} y usuario {}", tournamentId, userId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .filter(t -> t.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        // Validar que el torneo acepta tickets
        if (!tournament.getTournamentType().isPaid()) {
            throw new IllegalArgumentException("El torneo no acepta tickets de pago");
        }

        // Validar que el torneo no haya iniciado
        LocalDateTime now = LocalDateTime.now();
        if (tournament.getStartDate().isBefore(now)) {
            throw new IllegalArgumentException("No se pueden crear tickets para torneos que ya iniciaron");
        }

        // Generar código único del ticket
        String ticketCode = generateUniqueTicketCode();

        // Calcular fecha de expiración (24 horas antes del inicio del torneo)
        LocalDateTime expirationDate = tournament.getStartDate().minusHours(24);

        // Crear el ticket
        Ticket ticket = new Ticket(tournament, userId, ticketCode, price, expirationDate);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Generar código QR después de guardar para tener el ID
        String qrCode = qrCodeService.generateQrCode(savedTicket);
        savedTicket.setQrCode(qrCode);
        savedTicket = ticketRepository.save(savedTicket);

        logger.info("Ticket creado exitosamente: {}", savedTicket.getId());

        return ticketMapper.toDto(savedTicket);
    }

    /**
     * Obtiene un ticket por ID.
     * 
     * @param id ID del ticket
     * @return el ticket encontrado
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "tickets", key = "#id")
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long id) {
        logger.debug("Obteniendo ticket por ID: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .filter(t -> t.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));

        return ticketMapper.toDto(ticket);
    }

    /**
     * Obtiene un ticket por código.
     * 
     * @param code código del ticket
     * @return el ticket encontrado
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "tickets", key = "#code")
    @Transactional(readOnly = true)
    public TicketDto getTicketByCode(String code) {
        logger.debug("Obteniendo ticket por código: {}", code);

        Ticket ticket = ticketRepository.findByTicketCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + code));

        return ticketMapper.toDto(ticket);
    }

    /**
     * Obtiene tickets de un usuario.
     * 
     * @param userId ID del usuario
     * @param pageable información de paginación
     * @return página de tickets del usuario
     */
    @Cacheable(value = "userTickets", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<TicketDto> getUserTickets(Long userId, Pageable pageable) {
        logger.debug("Obteniendo tickets del usuario: {}", userId);

        List<Ticket> tickets = ticketRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        return convertListToPage(tickets, pageable);
    }

    /**
     * Obtiene tickets de un torneo.
     * 
     * @param tournamentId ID del torneo
     * @param pageable información de paginación
     * @return página de tickets del torneo
     */
    @Transactional(readOnly = true)
    public Page<TicketDto> getTournamentTickets(Long tournamentId, Pageable pageable) {
        logger.debug("Obteniendo tickets del torneo: {}", tournamentId);

        List<Ticket> tickets = ticketRepository.findByTournamentIdAndActiveTrueOrderByCreatedAtDesc(tournamentId);
        return convertListToPage(tickets, pageable);
    }

    /**
     * Obtiene tickets por estado.
     * 
     * @param status estado de los tickets
     * @param pageable información de paginación
     * @return página de tickets con el estado especificado
     */
    @Transactional(readOnly = true)
    public Page<TicketDto> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        logger.debug("Obteniendo tickets por estado: {}", status);

        List<Ticket> tickets = ticketRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(status);
        return convertListToPage(tickets, pageable);
    }

    /**
     * Marca un ticket como pagado.
     * 
     * @param ticketId ID del ticket
     * @param paymentReference referencia del pago
     * @return el ticket actualizado
     * @throws IllegalArgumentException si no se encuentra o no se puede pagar
     */
    @CacheEvict(value = {"tickets", "userTickets"}, allEntries = true)
    public TicketDto markTicketAsPaid(Long ticketId, String paymentReference) {
        logger.info("Marcando ticket como pagado: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .filter(t -> t.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        ticket.markAsPaid(paymentReference);
        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket marcado como pagado exitosamente: {}", savedTicket.getId());
        return ticketMapper.toDto(savedTicket);
    }

    /**
     * Marca un ticket como usado.
     * 
     * @param ticketCode código del ticket
     * @return el ticket actualizado
     * @throws IllegalArgumentException si no se encuentra o no se puede usar
     */
    @CacheEvict(value = {"tickets", "userTickets"}, allEntries = true)
    public TicketDto useTicket(String ticketCode) {
        logger.info("Usando ticket: {}", ticketCode);

        Ticket ticket = ticketRepository.findByTicketCodeAndActiveTrue(ticketCode)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketCode));

        if (!ticket.isValidForUse()) {
            throw new IllegalArgumentException("El ticket no es válido para uso");
        }

        ticket.markAsUsed();
        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket usado exitosamente: {}", savedTicket.getId());
        return ticketMapper.toDto(savedTicket);
    }

    /**
     * Cancela un ticket.
     * 
     * @param ticketId ID del ticket
     * @return el ticket cancelado
     * @throws IllegalArgumentException si no se encuentra o no se puede cancelar
     */
    @CacheEvict(value = {"tickets", "userTickets"}, allEntries = true)
    public TicketDto cancelTicket(Long ticketId) {
        logger.info("Cancelando ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .filter(t -> t.getActive())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        if (!ticket.canBeRefunded()) {
            throw new IllegalArgumentException("El ticket no puede ser cancelado");
        }

        ticket.cancel();
        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket cancelado exitosamente: {}", savedTicket.getId());
        return ticketMapper.toDto(savedTicket);
    }

    /**
     * Marca tickets expirados automáticamente.
     * 
     * @return número de tickets marcados como expirados
     */
    @CacheEvict(value = {"tickets", "userTickets"}, allEntries = true)
    public int markExpiredTickets() {
        logger.info("Marcando tickets expirados");

        List<Ticket> expiredTickets = ticketRepository.findExpiredTickets(LocalDateTime.now());
        
        for (Ticket ticket : expiredTickets) {
            ticket.markAsExpired();
        }

        if (!expiredTickets.isEmpty()) {
            ticketRepository.saveAll(expiredTickets);
        }

        logger.info("Marcados {} tickets como expirados", expiredTickets.size());
        return expiredTickets.size();
    }

    /**
     * Obtiene estadísticas de tickets.
     * 
     * @return estadísticas generales
     */
    @Transactional(readOnly = true)
    public TicketStatsDto getTicketStats() {
        logger.debug("Obteniendo estadísticas de tickets");

        // Contar tickets por estado manualmente
        List<Ticket> allTickets = ticketRepository.findAll();
        long totalActive = allTickets.stream().filter(t -> t.getActive()).count();
        long totalPaid = allTickets.stream().filter(t -> t.getActive() && t.getStatus() == TicketStatus.PAID).count();
        long totalUsed = allTickets.stream().filter(t -> t.getActive() && t.getStatus() == TicketStatus.USED).count();
        long totalExpired = allTickets.stream().filter(t -> t.getActive() && t.getStatus() == TicketStatus.EXPIRED).count();
        
        // Calcular ingresos totales
        BigDecimal totalRevenue = allTickets.stream()
                .filter(t -> t.getActive() && t.getStatus() == TicketStatus.PAID)
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TicketStatsDto(totalActive, totalPaid, totalUsed, totalExpired, totalRevenue);
    }

    /**
     * Valida un ticket por código QR.
     * 
     * @param ticketCode código del ticket a validar
     * @return información de validación del ticket
     */
    @Transactional(readOnly = true)
    public TicketValidationDto validateTicket(String ticketCode) {
        logger.debug("Validando ticket por código: {}", ticketCode);

        try {
            Ticket ticket = ticketRepository.findByTicketCodeAndActiveTrue(ticketCode)
                    .orElse(null);

            if (ticket == null) {
                return new TicketValidationDto(false, "Ticket no encontrado", null);
            }

            if (!ticket.isValidForUse()) {
                return new TicketValidationDto(false, "Ticket no válido para uso", ticketMapper.toDto(ticket));
            }

            return new TicketValidationDto(true, "Ticket válido", ticketMapper.toDto(ticket));

        } catch (Exception e) {
            logger.error("Error validando ticket: {}", e.getMessage());
            return new TicketValidationDto(false, "Error validando ticket", null);
        }
    }

    /**
     * Genera un código único para el ticket.
     * 
     * @return código único
     */
    private String generateUniqueTicketCode() {
        String code;
        do {
            code = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (ticketRepository.existsByTicketCode(code));
        
        return code;
    }

    /**
     * Convierte una lista de tickets a una página.
     * 
     * @param tickets lista de tickets
     * @param pageable información de paginación
     * @return página de tickets
     */
    private Page<TicketDto> convertListToPage(List<Ticket> tickets, Pageable pageable) {
        List<TicketDto> ticketDtos = tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), ticketDtos.size());
        
        List<TicketDto> pageContent = ticketDtos.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, ticketDtos.size());
    }

    /**
     * DTO para estadísticas de tickets.
     */
    public static class TicketStatsDto {
        private final long totalActiveTickets;
        private final long totalPaidTickets;
        private final long totalUsedTickets;
        private final long totalExpiredTickets;
        private final BigDecimal totalRevenue;

        public TicketStatsDto(long totalActiveTickets, long totalPaidTickets, 
                             long totalUsedTickets, long totalExpiredTickets, 
                             BigDecimal totalRevenue) {
            this.totalActiveTickets = totalActiveTickets;
            this.totalPaidTickets = totalPaidTickets;
            this.totalUsedTickets = totalUsedTickets;
            this.totalExpiredTickets = totalExpiredTickets;
            this.totalRevenue = totalRevenue;
        }

        public long getTotalActiveTickets() { return totalActiveTickets; }
        public long getTotalPaidTickets() { return totalPaidTickets; }
        public long getTotalUsedTickets() { return totalUsedTickets; }
        public long getTotalExpiredTickets() { return totalExpiredTickets; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }

    /**
     * DTO para validación de tickets.
     */
    public static class TicketValidationDto {
        private final boolean valid;
        private final String message;
        private final TicketDto ticket;

        public TicketValidationDto(boolean valid, String message, TicketDto ticket) {
            this.valid = valid;
            this.message = message;
            this.ticket = ticket;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public TicketDto getTicket() { return ticket; }
    }
} 