package com.esport.torneo.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para actualizar torneos existentes.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
public class TournamentUpdateDto {

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String description;

    @Positive(message = "El máximo de participantes debe ser positivo")
    private Integer maxParticipants;

    @DecimalMin(value = "0.0", message = "El precio de entrada no puede ser negativo")
    private BigDecimal entryFee;

    @DecimalMin(value = "0.0", message = "El pozo de premios no puede ser negativo")
    private BigDecimal prizePool;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationStart;
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
    public TournamentUpdateDto() {
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
        return "TournamentUpdateDto{" +
                "name='" + name + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", entryFee=" + entryFee +
                ", prizePool=" + prizePool +
                ", startDate=" + startDate +
                '}';
    }
} 