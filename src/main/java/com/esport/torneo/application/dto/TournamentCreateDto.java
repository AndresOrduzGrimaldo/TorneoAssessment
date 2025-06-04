package com.esport.torneo.application.dto;

import com.esport.torneo.domain.tournament.TournamentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para crear nuevos torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
public class TournamentCreateDto {

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String description;

    @NotNull(message = "El tipo de torneo es obligatorio")
    private TournamentType tournamentType = TournamentType.FREE;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El juego es obligatorio")
    private Long gameId;

    @NotNull(message = "El organizador es obligatorio")
    private Long organizerId;

    @NotNull(message = "El máximo de participantes es obligatorio")
    @Positive(message = "El máximo de participantes debe ser positivo")
    private Integer maxParticipants;

    @NotNull(message = "El precio de entrada es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de entrada no puede ser negativo")
    private BigDecimal entryFee = BigDecimal.ZERO;

    @NotNull(message = "El pozo de premios es obligatorio")
    @DecimalMin(value = "0.0", message = "El pozo de premios no puede ser negativo")
    private BigDecimal prizePool = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @DecimalMax(value = "1.0", message = "La comisión no puede ser mayor al 100%")
    private BigDecimal commissionRate;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDateTime endDate;

    @NotNull(message = "La fecha de inicio de registraciones es obligatoria")
    private LocalDateTime registrationStart;

    @NotNull(message = "La fecha de finalización de registraciones es obligatoria")
    private LocalDateTime registrationEnd;

    @Size(max = 255, message = "La URL del stream no puede exceder 255 caracteres")
    private String streamUrl;

    @Size(max = 30, message = "La plataforma de stream no puede exceder 30 caracteres")
    private String streamPlatform;

    private String rules;

    @Size(max = 255, message = "La URL del banner no puede exceder 255 caracteres")
    private String bannerImageUrl;

    /**
     * Constructor por defecto.
     */
    public TournamentCreateDto() {
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

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

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public BigDecimal getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(BigDecimal entryFee) {
        this.entryFee = entryFee;
    }

    public BigDecimal getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(BigDecimal prizePool) {
        this.prizePool = prizePool;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(LocalDateTime registrationStart) {
        this.registrationStart = registrationStart;
    }

    public LocalDateTime getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(LocalDateTime registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getStreamPlatform() {
        return streamPlatform;
    }

    public void setStreamPlatform(String streamPlatform) {
        this.streamPlatform = streamPlatform;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    @Override
    public String toString() {
        return "TournamentCreateDto{" +
                "name='" + name + '\'' +
                ", tournamentType=" + tournamentType +
                ", categoryId=" + categoryId +
                ", gameId=" + gameId +
                ", organizerId=" + organizerId +
                ", maxParticipants=" + maxParticipants +
                ", startDate=" + startDate +
                '}';
    }
} 