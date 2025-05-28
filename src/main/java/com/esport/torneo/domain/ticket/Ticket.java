package com.esport.torneo.domain.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.esport.torneo.domain.common.BaseEntity;
import com.esport.torneo.domain.tournament.Tournament;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa un ticket virtual para acceso a torneos de pago.
 * 
 * Los tickets incluyen:
 * - Código único de identificación
 * - Código QR para validación
 * - Información de precio y comisión
 * - Estados de ciclo de vida
 * - Fechas de compra, expiración y uso
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_code", columnList = "ticket_code", unique = true),
    @Index(name = "idx_ticket_tournament", columnList = "tournament_id"),
    @Index(name = "idx_ticket_user", columnList = "user_id"),
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_expiration", columnList = "expiration_date"),
    @Index(name = "idx_ticket_active", columnList = "active")
})
public class Ticket extends BaseEntity {

    /**
     * Estados posibles de un ticket.
     */
    public enum TicketStatus {
        RESERVED("Reservado"),
        PAID("Pagado"),
        USED("Usado"),
        EXPIRED("Expirado"),
        CANCELLED("Cancelado");

        private final String displayName;

        TicketStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Verifica si se puede transicionar a un nuevo estado.
         * 
         * @param newStatus el nuevo estado
         * @return true si la transición es válida
         */
        public boolean canTransitionTo(TicketStatus newStatus) {
            if (this == newStatus) {
                return false;
            }
            
            switch (this) {
                case RESERVED:
                    return newStatus == PAID || newStatus == CANCELLED || newStatus == EXPIRED;
                case PAID:
                    return newStatus == USED || newStatus == EXPIRED || newStatus == CANCELLED;
                case USED:
                case EXPIRED:
                case CANCELLED:
                    return false; // Estados finales
                default:
                    return false;
            }
        }

        /**
         * Verifica si el ticket está activo (puede ser usado).
         * 
         * @return true si está pagado
         */
        public boolean isActive() {
            return this == PAID;
        }
    }

    /**
     * Torneo al que pertenece este ticket.
     */
    @NotNull(message = "El torneo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /**
     * ID del usuario propietario del ticket.
     */
    @NotNull(message = "El usuario es obligatorio")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Código único del ticket.
     */
    @NotBlank(message = "El código del ticket es obligatorio")
    @Size(min = 8, max = 50, message = "El código debe tener entre 8 y 50 caracteres")
    @Column(name = "ticket_code", nullable = false, unique = true, length = 50)
    private String ticketCode;

    /**
     * Código QR en formato Base64.
     */
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    /**
     * Precio del ticket.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Comisión de la plataforma.
     */
    @NotNull(message = "La comisión es obligatoria")
    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @Column(name = "commission", nullable = false, precision = 10, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    /**
     * Estado actual del ticket.
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.RESERVED;

    /**
     * Fecha y hora de compra.
     */
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    /**
     * Fecha y hora de expiración.
     */
    @NotNull(message = "La fecha de expiración es obligatoria")
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    /**
     * Fecha y hora de uso.
     */
    @Column(name = "usage_date")
    private LocalDateTime usageDate;

    /**
     * Referencia del pago.
     */
    @Size(max = 100, message = "La referencia de pago no puede exceder 100 caracteres")
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    /**
     * Constructor por defecto para JPA.
     */
    protected Ticket() {
        super();
    }

    /**
     * Constructor para crear un nuevo ticket.
     * 
     * @param tournament el torneo
     * @param userId el ID del usuario
     * @param ticketCode el código único del ticket
     * @param price el precio del ticket
     * @param expirationDate la fecha de expiración
     */
    public Ticket(Tournament tournament, Long userId, String ticketCode, 
                  BigDecimal price, LocalDateTime expirationDate) {
        super();
        this.tournament = tournament;
        this.userId = userId;
        this.ticketCode = ticketCode;
        this.price = price;
        this.expirationDate = expirationDate;
        this.status = TicketStatus.RESERVED;
        
        calculateCommission();
        validateExpirationDate();
    }

    /**
     * Constructor completo.
     * 
     * @param tournament el torneo
     * @param userId el ID del usuario
     * @param ticketCode el código único del ticket
     * @param price el precio del ticket
     * @param expirationDate la fecha de expiración
     * @param qrCode el código QR
     */
    public Ticket(Tournament tournament, Long userId, String ticketCode, 
                  BigDecimal price, LocalDateTime expirationDate, String qrCode) {
        this(tournament, userId, ticketCode, price, expirationDate);
        this.qrCode = qrCode;
    }

    // ======================================================================
    // MÉTODOS DE NEGOCIO
    // ======================================================================

    /**
     * Calcula la comisión basada en la tasa del torneo.
     */
    private void calculateCommission() {
        if (tournament != null && price != null) {
            this.commission = price.multiply(tournament.getCommissionRate());
        }
    }

    /**
     * Valida que la fecha de expiración sea futura.
     */
    private void validateExpirationDate() {
        if (expirationDate != null && expirationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de expiración debe ser futura");
        }
    }

    /**
     * Marca el ticket como pagado.
     * 
     * @param paymentReference la referencia del pago
     * @throws IllegalStateException si el ticket no puede ser pagado
     */
    public void markAsPaid(String paymentReference) {
        if (!status.canTransitionTo(TicketStatus.PAID)) {
            throw new IllegalStateException("No se puede marcar como pagado el ticket en estado: " + status);
        }
        
        if (isExpired()) {
            throw new IllegalStateException("No se puede pagar un ticket expirado");
        }
        
        this.status = TicketStatus.PAID;
        this.purchaseDate = LocalDateTime.now();
        this.paymentReference = paymentReference;
    }

    /**
     * Marca el ticket como usado.
     * 
     * @throws IllegalStateException si el ticket no puede ser usado
     */
    public void markAsUsed() {
        if (!status.canTransitionTo(TicketStatus.USED)) {
            throw new IllegalStateException("No se puede usar el ticket en estado: " + status);
        }
        
        if (isExpired()) {
            throw new IllegalStateException("No se puede usar un ticket expirado");
        }
        
        this.status = TicketStatus.USED;
        this.usageDate = LocalDateTime.now();
    }

    /**
     * Cancela el ticket.
     * 
     * @throws IllegalStateException si el ticket no puede ser cancelado
     */
    public void cancel() {
        if (!status.canTransitionTo(TicketStatus.CANCELLED)) {
            throw new IllegalStateException("No se puede cancelar el ticket en estado: " + status);
        }
        
        this.status = TicketStatus.CANCELLED;
    }

    /**
     * Marca el ticket como expirado.
     * 
     * @throws IllegalStateException si el ticket no puede expirar
     */
    public void markAsExpired() {
        if (!status.canTransitionTo(TicketStatus.EXPIRED)) {
            throw new IllegalStateException("No se puede expirar el ticket en estado: " + status);
        }
        
        this.status = TicketStatus.EXPIRED;
    }

    /**
     * Verifica si el ticket ha expirado.
     * 
     * @return true si ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }

    /**
     * Verifica si el ticket es válido para uso.
     * 
     * @return true si está pagado y no ha expirado
     */
    public boolean isValidForUse() {
        return status == TicketStatus.PAID && !isExpired();
    }

    /**
     * Verifica si el ticket puede ser reembolsado.
     * 
     * @return true si está pagado y no ha sido usado
     */
    public boolean canBeRefunded() {
        return status == TicketStatus.PAID && usageDate == null;
    }

    /**
     * Calcula el monto neto (precio - comisión).
     * 
     * @return el monto neto
     */
    public BigDecimal getNetAmount() {
        return price.subtract(commission);
    }

    /**
     * Actualiza el código QR del ticket.
     * 
     * @param qrCode el nuevo código QR
     */
    public void updateQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDateTime usageDate) {
        this.usageDate = usageDate;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + getId() +
                ", ticketCode='" + ticketCode + '\'' +
                ", tournamentId=" + (tournament != null ? tournament.getId() : null) +
                ", userId=" + userId +
                ", price=" + price +
                ", status=" + status +
                ", expirationDate=" + expirationDate +
                '}';
    }
} 