package com.esport.torneo.domain.tournament;

import java.time.LocalDateTime;

import com.esport.torneo.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa una etapa o fase de un torneo.
 * 
 * Los torneos pueden tener múltiples etapas como:
 * - Clasificatorias
 * - Octavos de final
 * - Cuartos de final
 * - Semifinales
 * - Final
 * 
 * Forma parte del agregado Tournament.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "tournament_stages",
       uniqueConstraints = @UniqueConstraint(name = "uk_tournament_stage_order", 
                                           columnNames = {"tournament_id", "stage_order"}),
       indexes = {
    @Index(name = "idx_stage_tournament", columnList = "tournament_id"),
    @Index(name = "idx_stage_order", columnList = "stage_order"),
    @Index(name = "idx_stage_status", columnList = "status")
})
public class TournamentStage extends BaseEntity {

    /**
     * Estados posibles de una etapa.
     */
    public enum StageStatus {
        PENDING("Pendiente"),
        IN_PROGRESS("En Progreso"),
        COMPLETED("Completada"),
        CANCELLED("Cancelada");

        private final String displayName;

        StageStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean canTransitionTo(StageStatus newStatus) {
            if (this == newStatus) {
                return false;
            }
            
            switch (this) {
                case PENDING:
                    return newStatus == IN_PROGRESS || newStatus == CANCELLED;
                case IN_PROGRESS:
                    return newStatus == COMPLETED || newStatus == CANCELLED;
                case COMPLETED:
                case CANCELLED:
                    return false; // Estados finales
                default:
                    return false;
            }
        }
    }

    /**
     * Torneo al que pertenece esta etapa.
     */
    @NotNull(message = "El torneo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /**
     * Nombre de la etapa.
     */
    @NotBlank(message = "El nombre de la etapa es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Orden de la etapa dentro del torneo.
     */
    @NotNull(message = "El orden de la etapa es obligatorio")
    @Positive(message = "El orden debe ser positivo")
    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    /**
     * Fecha y hora de inicio de la etapa.
     */
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Fecha y hora de finalización de la etapa.
     */
    @NotNull(message = "La fecha de finalización es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Estado actual de la etapa.
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StageStatus status = StageStatus.PENDING;

    /**
     * Descripción de la etapa.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Constructor por defecto para JPA.
     */
    protected TournamentStage() {
        super();
    }

    /**
     * Constructor para crear una nueva etapa.
     * 
     * @param tournament el torneo
     * @param name nombre de la etapa
     * @param stageOrder orden de la etapa
     * @param startDate fecha de inicio
     * @param endDate fecha de finalización
     */
    public TournamentStage(Tournament tournament, String name, Integer stageOrder,
                          LocalDateTime startDate, LocalDateTime endDate) {
        super();
        this.tournament = tournament;
        this.name = name;
        this.stageOrder = stageOrder;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = StageStatus.PENDING;
        
        validateDates();
    }

    /**
     * Constructor completo.
     * 
     * @param tournament el torneo
     * @param name nombre de la etapa
     * @param stageOrder orden de la etapa
     * @param startDate fecha de inicio
     * @param endDate fecha de finalización
     * @param description descripción de la etapa
     */
    public TournamentStage(Tournament tournament, String name, Integer stageOrder,
                          LocalDateTime startDate, LocalDateTime endDate, String description) {
        this(tournament, name, stageOrder, startDate, endDate);
        this.description = description;
    }

    /**
     * Valida las fechas de la etapa.
     */
    private void validateDates() {
        if (endDate != null && startDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la de inicio");
        }
    }

    /**
     * Inicia la etapa cambiando su estado a IN_PROGRESS.
     * 
     * @throws IllegalStateException si la etapa no puede iniciarse
     */
    public void start() {
        if (!status.canTransitionTo(StageStatus.IN_PROGRESS)) {
            throw new IllegalStateException("No se puede iniciar la etapa en su estado actual: " + status);
        }
        
        this.status = StageStatus.IN_PROGRESS;
    }

    /**
     * Completa la etapa cambiando su estado a COMPLETED.
     * 
     * @throws IllegalStateException si la etapa no puede completarse
     */
    public void complete() {
        if (!status.canTransitionTo(StageStatus.COMPLETED)) {
            throw new IllegalStateException("No se puede completar la etapa en su estado actual: " + status);
        }
        
        this.status = StageStatus.COMPLETED;
    }

    /**
     * Cancela la etapa cambiando su estado a CANCELLED.
     * 
     * @throws IllegalStateException si la etapa no puede cancelarse
     */
    public void cancel() {
        if (!status.canTransitionTo(StageStatus.CANCELLED)) {
            throw new IllegalStateException("No se puede cancelar la etapa en su estado actual: " + status);
        }
        
        this.status = StageStatus.CANCELLED;
    }

    /**
     * Verifica si la etapa está activa.
     * 
     * @return true si está pendiente o en progreso
     */
    public boolean isActive() {
        return status == StageStatus.PENDING || status == StageStatus.IN_PROGRESS;
    }

    /**
     * Verifica si la etapa ha terminado.
     * 
     * @return true si está completada o cancelada
     */
    public boolean isFinished() {
        return status == StageStatus.COMPLETED || status == StageStatus.CANCELLED;
    }

    /**
     * Actualiza la información de la etapa.
     * 
     * @param name nuevo nombre
     * @param startDate nueva fecha de inicio
     * @param endDate nueva fecha de finalización
     * @param description nueva descripción
     */
    public void updateStage(String name, LocalDateTime startDate, LocalDateTime endDate, String description) {
        if (status != StageStatus.PENDING) {
            throw new IllegalStateException("Solo se puede editar la etapa en estado pendiente");
        }
        
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        
        validateDates();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
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

    public StageStatus getStatus() {
        return status;
    }

    public void setStatus(StageStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TournamentStage{" +
                "id=" + getId() +
                ", tournamentId=" + (tournament != null ? tournament.getId() : null) +
                ", name='" + name + '\'' +
                ", stageOrder=" + stageOrder +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
} 