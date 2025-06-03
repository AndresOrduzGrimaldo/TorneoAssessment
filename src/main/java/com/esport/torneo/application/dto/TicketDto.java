package com.esport.torneo.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.esport.torneo.domain.ticket.Ticket.TicketStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferencia de datos de tickets.
 * 
 * Utilizado en las operaciones de API REST para
 * enviar y recibir información de tickets de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDto {

    private Long id;

    @NotBlank(message = "El código del ticket es obligatorio")
    @Size(min = 8, max = 50, message = "El código debe tener entre 8 y 50 caracteres")
    private String code;

    @NotNull(message = "El ID del torneo es obligatorio")
    private Long tournamentId;

    @Size(max = 100, message = "El nombre del torneo no puede exceder 100 caracteres")
    private String tournamentName;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal price;

    @NotNull(message = "El estado es obligatorio")
    private TicketStatus status;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    @Size(max = 255, message = "La URL del código QR no puede exceder 255 caracteres")
    private String qrCodeUrl;

    @Size(max = 100, message = "El email del comprador no puede exceder 100 caracteres")
    @Email(message = "El email debe tener un formato válido")
    private String buyerEmail;

    @Size(max = 100, message = "El nombre del comprador no puede exceder 100 caracteres")
    private String buyerName;

    private LocalDateTime purchaseDate;
    private LocalDateTime usedDate;
    private LocalDateTime expirationDate;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos calculados
    private Boolean isExpired;
    private Boolean isUsed;
    private Boolean isValid;
    private Boolean hasQrCode;
    private Long daysUntilExpiration;

    /**
     * Constructor por defecto.
     */
    public TicketDto() {
    }

    /**
     * Constructor para crear DTO básico.
     * 
     * @param code código del ticket
     * @param tournamentId ID del torneo
     * @param price precio del ticket
     * @param status estado del ticket
     */
    public TicketDto(String code, Long tournamentId, BigDecimal price, TicketStatus status) {
        this.code = code;
        this.tournamentId = tournamentId;
        this.price = price;
        this.status = status;
    }

    /**
     * Constructor completo.
     * 
     * @param id ID del ticket
     * @param code código del ticket
     * @param tournamentId ID del torneo
     * @param tournamentName nombre del torneo
     * @param price precio del ticket
     * @param status estado del ticket
     * @param notes notas adicionales
     * @param qrCodeUrl URL del código QR
     * @param buyerEmail email del comprador
     * @param buyerName nombre del comprador
     * @param purchaseDate fecha de compra
     * @param usedDate fecha de uso
     * @param expirationDate fecha de expiración
     * @param active estado activo
     * @param createdAt fecha de creación
     * @param updatedAt fecha de actualización
     */
    public TicketDto(Long id, String code, Long tournamentId, String tournamentName,
                     BigDecimal price, TicketStatus status, String notes, String qrCodeUrl,
                     String buyerEmail, String buyerName, LocalDateTime purchaseDate,
                     LocalDateTime usedDate, LocalDateTime expirationDate,
                     Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.price = price;
        this.status = status;
        this.notes = notes;
        this.qrCodeUrl = qrCodeUrl;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.purchaseDate = purchaseDate;
        this.usedDate = usedDate;
        this.expirationDate = expirationDate;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Calcular campos derivados
        calculateDerivedFields();
    }

    /**
     * Calcula los campos derivados del ticket.
     */
    private void calculateDerivedFields() {
        LocalDateTime now = LocalDateTime.now();
        
        this.isExpired = expirationDate != null && expirationDate.isBefore(now);
        this.isUsed = usedDate != null;
        this.isValid = status == TicketStatus.PAID && !isExpired && !isUsed;
        this.hasQrCode = qrCodeUrl != null && !qrCodeUrl.trim().isEmpty();
        
        if (expirationDate != null && !isExpired) {
            this.daysUntilExpiration = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), expirationDate.toLocalDate());
        } else {
            this.daysUntilExpiration = 0L;
        }
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
        this.hasQrCode = qrCodeUrl != null && !qrCodeUrl.trim().isEmpty();
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(LocalDateTime usedDate) {
        this.usedDate = usedDate;
        this.isUsed = usedDate != null;
        calculateDerivedFields();
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        calculateDerivedFields();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Boolean getHasQrCode() {
        return hasQrCode;
    }

    public void setHasQrCode(Boolean hasQrCode) {
        this.hasQrCode = hasQrCode;
    }

    public Long getDaysUntilExpiration() {
        return daysUntilExpiration;
    }

    public void setDaysUntilExpiration(Long daysUntilExpiration) {
        this.daysUntilExpiration = daysUntilExpiration;
    }

    @Override
    public String toString() {
        return "TicketDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", tournamentId=" + tournamentId +
                ", price=" + price +
                ", status=" + status +
                ", buyerEmail='" + buyerEmail + '\'' +
                ", isValid=" + isValid +
                '}';
    }
} 