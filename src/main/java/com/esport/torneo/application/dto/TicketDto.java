package com.esport.torneo.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.esport.torneo.domain.ticket.Ticket.TicketStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para representar un ticket de torneo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de un ticket de torneo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDto {

    @Schema(description = "ID único del ticket", example = "1")
    private Long id;

    @Schema(description = "Código único del ticket", example = "TKT-2024-ABC123")
    @NotBlank(message = "El código del ticket es obligatorio")
    @Size(min = 8, max = 50, message = "El código debe tener entre 8 y 50 caracteres")
    private String ticketCode;

    @Schema(description = "Estado del ticket", example = "PAID")
    @NotNull(message = "El estado es obligatorio")
    private TicketStatus status;

    @Schema(description = "Precio del ticket", example = "25.00", required = true)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal price;

    @Schema(description = "Comisión calculada", example = "2.50")
    private BigDecimal commission;

    @Schema(description = "Porcentaje de comisión aplicado", example = "10.0")
    private BigDecimal commissionPercentage;

    @Schema(description = "Código QR del ticket en Base64")
    private String qrCodeBase64;

    @Schema(description = "URL del código QR")
    @Size(max = 255, message = "La URL del código QR no puede exceder 255 caracteres")
    private String qrCodeUrl;

    @Schema(description = "Fecha de compra", example = "2024-01-15T10:30:00")
    private LocalDateTime purchaseDate;

    @Schema(description = "Fecha de expiración", example = "2024-03-15T23:59:59")
    private LocalDateTime expirationDate;

    @Schema(description = "Fecha de uso del ticket", example = "2024-03-15T14:00:00")
    private LocalDateTime usedDate;

    @Schema(description = "Fecha de validación", example = "2024-03-15T14:00:00")
    private LocalDateTime validatedDate;

    @Schema(description = "ID del torneo", example = "1", required = true)
    @NotNull(message = "El torneo es obligatorio")
    private Long tournamentId;

    @Schema(description = "Nombre del torneo", example = "Copa Mundial CS2 2024")
    @Size(max = 100, message = "El nombre del torneo no puede exceder 100 caracteres")
    private String tournamentName;

    @Schema(description = "ID del usuario comprador", example = "1", required = true)
    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @Schema(description = "Nombre del usuario", example = "johndoe")
    @Size(max = 100, message = "El nombre del usuario no puede exceder 100 caracteres")
    private String username;

    @Schema(description = "Email del usuario", example = "user@example.com")
    @Size(max = 100, message = "El email del usuario no puede exceder 100 caracteres")
    @Email(message = "El email debe tener un formato válido")
    private String userEmail;

    @Schema(description = "Información del método de pago")
    private String paymentMethod;

    @Schema(description = "ID de transacción externa")
    private String externalTransactionId;

    @Schema(description = "Notas adicionales")
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    @Schema(description = "Indica si el ticket ha expirado", example = "false")
    private Boolean isExpired;

    @Schema(description = "Indica si el ticket ha sido usado", example = "false")
    private Boolean isUsed;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    // Campos calculados
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
     * @param ticketCode código del ticket
     * @param tournamentId ID del torneo
     * @param price precio del ticket
     * @param status estado del ticket
     */
    public TicketDto(String ticketCode, Long tournamentId, BigDecimal price, TicketStatus status) {
        this.ticketCode = ticketCode;
        this.tournamentId = tournamentId;
        this.price = price;
        this.status = status;
    }

    /**
     * Constructor completo.
     * 
     * @param id ID del ticket
     * @param ticketCode código del ticket
     * @param status estado del ticket
     * @param price precio del ticket
     * @param commission comisión calculada
     * @param commissionPercentage porcentaje de comisión aplicado
     * @param qrCodeBase64 código QR en Base64
     * @param qrCodeUrl URL del código QR
     * @param purchaseDate fecha de compra
     * @param expirationDate fecha de expiración
     * @param usedDate fecha de uso
     * @param validatedDate fecha de validación
     * @param tournamentId ID del torneo
     * @param tournamentName nombre del torneo
     * @param userId ID del usuario comprador
     * @param username nombre del usuario
     * @param userEmail email del usuario
     * @param paymentMethod información del método de pago
     * @param externalTransactionId ID de transacción externa
     * @param notes notas adicionales
     * @param isExpired indica si el ticket ha expirado
     * @param isUsed indica si el ticket ha sido usado
     * @param createdAt fecha de creación
     * @param updatedAt fecha de actualización
     */
    public TicketDto(Long id, String ticketCode, TicketStatus status, BigDecimal price, BigDecimal commission,
                    BigDecimal commissionPercentage, String qrCodeBase64, String qrCodeUrl,
                    LocalDateTime purchaseDate, LocalDateTime expirationDate, LocalDateTime usedDate,
                    LocalDateTime validatedDate, Long tournamentId, String tournamentName,
                    Long userId, String username, String userEmail, String paymentMethod,
                    String externalTransactionId, String notes, Boolean isExpired, Boolean isUsed,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ticketCode = ticketCode;
        this.status = status;
        this.price = price;
        this.commission = commission;
        this.commissionPercentage = commissionPercentage;
        this.qrCodeBase64 = qrCodeBase64;
        this.qrCodeUrl = qrCodeUrl;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
        this.usedDate = usedDate;
        this.validatedDate = validatedDate;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.paymentMethod = paymentMethod;
        this.externalTransactionId = externalTransactionId;
        this.notes = notes;
        this.isExpired = isExpired;
        this.isUsed = isUsed;
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

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
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

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
        this.hasQrCode = qrCodeUrl != null && !qrCodeUrl.trim().isEmpty();
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
        calculateDerivedFields();
    }

    public LocalDateTime getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(LocalDateTime usedDate) {
        this.usedDate = usedDate;
        this.isUsed = usedDate != null;
        calculateDerivedFields();
    }

    public LocalDateTime getValidatedDate() {
        return validatedDate;
    }

    public void setValidatedDate(LocalDateTime validatedDate) {
        this.validatedDate = validatedDate;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    @Override
    public String toString() {
        return "TicketDto{" +
                "id=" + id +
                ", ticketCode='" + ticketCode + '\'' +
                ", status=" + status +
                ", price=" + price +
                ", tournamentId=" + tournamentId +
                ", userId=" + userId +
                ", purchaseDate=" + purchaseDate +
                '}';
    }
} 