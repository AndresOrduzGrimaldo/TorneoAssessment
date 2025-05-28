package com.esport.torneo.domain.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.domain.tournament.TournamentType;

/**
 * Servicio de dominio para gestionar la lógica de negocio de tickets.
 * 
 * Implementa las reglas de negocio para:
 * - Creación de tickets
 * - Validación de reglas de negocio
 * - Generación de códigos únicos
 * - Cálculo de fechas de expiración
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
public class TicketService {

    private final QrCodeService qrCodeService;

    @Autowired
    public TicketService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    /**
     * Crea un nuevo ticket para un torneo de pago.
     * 
     * @param tournament el torneo
     * @param userId el ID del usuario
     * @return el ticket creado
     * @throws IllegalArgumentException si el torneo no es de pago
     */
    public Ticket createTicket(Tournament tournament, Long userId) {
        validateTournamentForTicket(tournament);
        
        String ticketCode = generateUniqueTicketCode();
        BigDecimal price = tournament.getEntryFee();
        LocalDateTime expirationDate = calculateExpirationDate(tournament);
        
        Ticket ticket = new Ticket(tournament, userId, ticketCode, price, expirationDate);
        
        // Generar código QR
        String qrCode = qrCodeService.generateQrCode(ticket);
        ticket.updateQrCode(qrCode);
        
        return ticket;
    }

    /**
     * Procesa el pago de un ticket.
     * 
     * @param ticket el ticket a pagar
     * @param paymentReference la referencia del pago
     * @throws IllegalStateException si el ticket no puede ser pagado
     */
    public void processPayment(Ticket ticket, String paymentReference) {
        validateTicketForPayment(ticket);
        ticket.markAsPaid(paymentReference);
    }

    /**
     * Valida un ticket para su uso.
     * 
     * @param ticket el ticket a validar
     * @return true si el ticket es válido para uso
     */
    public boolean validateTicketForUse(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        
        return ticket.isValidForUse();
    }

    /**
     * Marca un ticket como usado.
     * 
     * @param ticket el ticket a marcar como usado
     * @throws IllegalStateException si el ticket no puede ser usado
     */
    public void useTicket(Ticket ticket) {
        if (!validateTicketForUse(ticket)) {
            throw new IllegalStateException("El ticket no es válido para uso");
        }
        
        ticket.markAsUsed();
    }

    /**
     * Cancela un ticket y procesa el reembolso si aplica.
     * 
     * @param ticket el ticket a cancelar
     * @return true si se puede reembolsar
     */
    public boolean cancelTicket(Ticket ticket) {
        boolean canRefund = ticket.canBeRefunded();
        ticket.cancel();
        return canRefund;
    }

    /**
     * Procesa la expiración automática de tickets.
     * 
     * @param ticket el ticket a verificar
     */
    public void processExpiration(Ticket ticket) {
        if (ticket.isExpired() && ticket.getStatus().canTransitionTo(Ticket.TicketStatus.EXPIRED)) {
            ticket.markAsExpired();
        }
    }

    /**
     * Calcula las métricas de un ticket.
     * 
     * @param ticket el ticket
     * @return las métricas del ticket
     */
    public TicketMetrics calculateTicketMetrics(Ticket ticket) {
        return new TicketMetrics(
            ticket.getPrice(),
            ticket.getCommission(),
            ticket.getNetAmount(),
            ticket.getStatus(),
            ticket.isExpired(),
            ticket.isValidForUse()
        );
    }

    // ======================================================================
    // MÉTODOS PRIVADOS
    // ======================================================================

    /**
     * Valida que el torneo sea elegible para tickets.
     * 
     * @param tournament el torneo a validar
     * @throws IllegalArgumentException si el torneo no es válido
     */
    private void validateTournamentForTicket(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("El torneo no puede ser nulo");
        }
        
        if (tournament.getTournamentType() != TournamentType.PAID) {
            throw new IllegalArgumentException("Solo se pueden crear tickets para torneos de pago");
        }
        
        if (tournament.getEntryFee() == null || tournament.getEntryFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El torneo debe tener un precio de entrada válido");
        }
        
        if (!tournament.isRegistrationOpen()) {
            throw new IllegalArgumentException("El período de registración del torneo no está abierto");
        }
        
        if (!tournament.hasAvailableSlots()) {
            throw new IllegalArgumentException("El torneo no tiene cupos disponibles");
        }
    }

    /**
     * Valida que un ticket pueda ser pagado.
     * 
     * @param ticket el ticket a validar
     * @throws IllegalStateException si el ticket no puede ser pagado
     */
    private void validateTicketForPayment(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalStateException("El ticket no puede ser nulo");
        }
        
        if (ticket.isExpired()) {
            throw new IllegalStateException("No se puede pagar un ticket expirado");
        }
        
        if (ticket.getStatus() != Ticket.TicketStatus.RESERVED) {
            throw new IllegalStateException("Solo se pueden pagar tickets en estado reservado");
        }
    }

    /**
     * Genera un código único para el ticket.
     * 
     * @return el código único
     */
    private String generateUniqueTicketCode() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Calcula la fecha de expiración del ticket.
     * 
     * @param tournament el torneo
     * @return la fecha de expiración
     */
    private LocalDateTime calculateExpirationDate(Tournament tournament) {
        // Los tickets expiran 1 hora antes del inicio del torneo
        return tournament.getStartDate().minusHours(1);
    }

    /**
     * Clase para encapsular las métricas de un ticket.
     */
    public static class TicketMetrics {
        private final BigDecimal price;
        private final BigDecimal commission;
        private final BigDecimal netAmount;
        private final Ticket.TicketStatus status;
        private final boolean expired;
        private final boolean validForUse;

        public TicketMetrics(BigDecimal price, BigDecimal commission, BigDecimal netAmount,
                           Ticket.TicketStatus status, boolean expired, boolean validForUse) {
            this.price = price;
            this.commission = commission;
            this.netAmount = netAmount;
            this.status = status;
            this.expired = expired;
            this.validForUse = validForUse;
        }

        // Getters
        public BigDecimal getPrice() { return price; }
        public BigDecimal getCommission() { return commission; }
        public BigDecimal getNetAmount() { return netAmount; }
        public Ticket.TicketStatus getStatus() { return status; }
        public boolean isExpired() { return expired; }
        public boolean isValidForUse() { return validForUse; }
    }
} 