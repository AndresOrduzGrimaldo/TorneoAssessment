package com.esport.torneo.application.dto;

import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.domain.tournament.TournamentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar un torneo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de un torneo")
public class TournamentDto {

    @Schema(description = "ID único del torneo", example = "1")
    private Long id;

    @Schema(description = "Nombre del torneo", example = "Copa Mundial CS2 2024", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @Schema(description = "Descripción del torneo", example = "Torneo mundial de Counter-Strike 2")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String description;

    @Schema(description = "Tipo de torneo", example = "PAID", required = true)
    @NotNull(message = "El tipo de torneo es obligatorio")
    private TournamentType type;

    @Schema(description = "Estado del torneo", example = "PUBLISHED")
    private TournamentStatus status;

    @Schema(description = "Fecha de inicio del torneo", example = "2024-03-15T14:00:00", required = true)
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startDateTime;

    @Schema(description = "Fecha de fin del torneo", example = "2024-03-15T18:00:00", required = true)
    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime endDateTime;

    @Schema(description = "Fecha límite de registro", example = "2024-03-14T23:59:59", required = true)
    @NotNull(message = "La fecha límite de registro es obligatoria")
    @Future(message = "La fecha límite debe ser futura")
    private LocalDateTime registrationDeadline;

    @Schema(description = "Máximo número de participantes", example = "64", required = true)
    @NotNull(message = "El máximo de participantes es obligatorio")
    @Positive(message = "El máximo de participantes debe ser positivo")
    @Max(value = 1000, message = "El máximo de participantes no puede exceder 1000")
    private Integer maxParticipants;

    @Schema(description = "Precio del ticket (para torneos de pago)", example = "25.00")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal ticketPrice;

    @Schema(description = "Comisión del organizador (%)", example = "10.0")
    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @DecimalMax(value = "50.0", message = "La comisión no puede exceder 50%")
    private BigDecimal commissionPercentage;

    @Schema(description = "Reglas del torneo")
    @Size(max = 5000, message = "Las reglas no pueden exceder 5000 caracteres")
    private String rules;

    @Schema(description = "Premios del torneo")
    @Size(max = 2000, message = "Los premios no pueden exceder 2000 caracteres")
    private String prizes;

    @Schema(description = "URL de transmisión en vivo")
    @Size(max = 500, message = "La URL de stream no puede exceder 500 caracteres")
    private String streamUrl;

    @Schema(description = "URL de Discord")
    @Size(max = 500, message = "La URL de Discord no puede exceder 500 caracteres")
    private String discordUrl;

    @Schema(description = "ID del juego", example = "1", required = true)
    @NotNull(message = "El juego es obligatorio")
    private Long gameId;

    @Schema(description = "Nombre del juego", example = "Counter-Strike 2")
    private String gameName;

    @Schema(description = "ID de la categoría", example = "1")
    private Long categoryId;

    @Schema(description = "Nombre de la categoría", example = "FPS")
    private String categoryName;

    @Schema(description = "ID del organizador", example = "1", required = true)
    @NotNull(message = "El organizador es obligatorio")
    private Long organizerId;

    @Schema(description = "Nombre del organizador", example = "Pro Gaming Events")
    private String organizerName;

    @Schema(description = "Número actual de participantes", example = "32")
    private Integer currentParticipants;

    @Schema(description = "Lista de participantes")
    private List<TournamentParticipantDto> participants;

    @Schema(description = "Lista de etapas del torneo")
    private List<TournamentStageDto> stages;

    @Schema(description = "Indica si el torneo está completo", example = "false")
    private Boolean isFull;

    @Schema(description = "Indica si las inscripciones están abiertas", example = "true")
    private Boolean registrationOpen;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto.
     */
    public TournamentDto() {
    }

    /**
     * Constructor completo.
     */
    public TournamentDto(Long id, String name, String description, TournamentType type, TournamentStatus status,
                        LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime registrationDeadline,
                        Integer maxParticipants, BigDecimal ticketPrice, BigDecimal commissionPercentage,
                        String rules, String prizes, String streamUrl, String discordUrl,
                        Long gameId, String gameName, Long categoryId, String categoryName,
                        Long organizerId, String organizerName, Integer currentParticipants,
                        Boolean isFull, Boolean registrationOpen, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = status;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.registrationDeadline = registrationDeadline;
        this.maxParticipants = maxParticipants;
        this.ticketPrice = ticketPrice;
        this.commissionPercentage = commissionPercentage;
        this.rules = rules;
        this.prizes = prizes;
        this.streamUrl = streamUrl;
        this.discordUrl = discordUrl;
        this.gameId = gameId;
        this.gameName = gameName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.currentParticipants = currentParticipants;
        this.isFull = isFull;
        this.registrationOpen = registrationOpen;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TournamentType getType() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type = type;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getPrizes() {
        return prizes;
    }

    public void setPrizes(String prizes) {
        this.prizes = prizes;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getDiscordUrl() {
        return discordUrl;
    }

    public void setDiscordUrl(String discordUrl) {
        this.discordUrl = discordUrl;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public List<TournamentParticipantDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<TournamentParticipantDto> participants) {
        this.participants = participants;
    }

    public List<TournamentStageDto> getStages() {
        return stages;
    }

    public void setStages(List<TournamentStageDto> stages) {
        this.stages = stages;
    }

    public Boolean getIsFull() {
        return isFull;
    }

    public void setIsFull(Boolean isFull) {
        this.isFull = isFull;
    }

    public Boolean getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(Boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
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
        return "TournamentDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", maxParticipants=" + maxParticipants +
                ", currentParticipants=" + currentParticipants +
                ", ticketPrice=" + ticketPrice +
                ", gameId=" + gameId +
                ", organizerId=" + organizerId +
                '}';
    }
} 