package com.esport.torneo.application.dto;

import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.domain.tournament.TournamentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de torneos (respuestas).
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentDto {

    private Long id;
    private String name;
    private String description;
    private TournamentType tournamentType;
    private TournamentStatus status;
    private CategoryDto category;
    private GameDto game;
    private Long organizerId;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal entryFee;
    private BigDecimal prizePool;
    private BigDecimal commissionRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private String streamUrl;
    private String streamPlatform;
    private String rules;
    private String bannerImageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campos calculados
    private Boolean isRegistrationOpen;
    private Boolean hasAvailableSlots;
    private BigDecimal totalCommission;

    /**
     * Constructor por defecto.
     */
    public TournamentDto() {
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

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public GameDto getGame() {
        return game;
    }

    public void setGame(GameDto game) {
        this.game = game;
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

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
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

    public Boolean getIsRegistrationOpen() {
        return isRegistrationOpen;
    }

    public void setIsRegistrationOpen(Boolean isRegistrationOpen) {
        this.isRegistrationOpen = isRegistrationOpen;
    }

    public Boolean getHasAvailableSlots() {
        return hasAvailableSlots;
    }

    public void setHasAvailableSlots(Boolean hasAvailableSlots) {
        this.hasAvailableSlots = hasAvailableSlots;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }

    @Override
    public String toString() {
        return "TournamentDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tournamentType=" + tournamentType +
                ", status=" + status +
                ", maxParticipants=" + maxParticipants +
                ", currentParticipants=" + currentParticipants +
                ", startDate=" + startDate +
                '}';
    }
} 