package com.esport.torneo.application.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar una etapa de torneo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de una etapa de torneo")
public class TournamentStageDto {

    @Schema(description = "ID único de la etapa", example = "1")
    private Long id;

    @Schema(description = "Nombre de la etapa", example = "Cuartos de Final")
    private String name;

    @Schema(description = "Descripción de la etapa", example = "Fase eliminatoria de cuartos de final")
    private String description;

    @Schema(description = "Orden de la etapa en el torneo", example = "3")
    private Integer stageOrder;

    @Schema(description = "Estado de la etapa", example = "COMPLETED")
    private String status;

    @Schema(description = "Fecha de inicio de la etapa", example = "2024-03-15T10:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "Fecha de fin de la etapa", example = "2024-03-15T14:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Número máximo de participantes en esta etapa", example = "8")
    private Integer maxParticipants;

    @Schema(description = "Número actual de participantes", example = "8")
    private Integer currentParticipants;

    /**
     * Constructor por defecto.
     */
    public TournamentStageDto() {
    }

    /**
     * Constructor completo.
     */
    public TournamentStageDto(Long id, String name, String description, Integer stageOrder, 
                             String status, LocalDateTime startDateTime, LocalDateTime endDateTime,
                             Integer maxParticipants, Integer currentParticipants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stageOrder = stageOrder;
        this.status = status;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
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

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    @Override
    public String toString() {
        return "TournamentStageDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stageOrder=" + stageOrder +
                ", status='" + status + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                '}';
    }
} 