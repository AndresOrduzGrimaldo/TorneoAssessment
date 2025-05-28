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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa un participante registrado en un torneo.
 * 
 * Forma parte del agregado Tournament y contiene la información
 * de registro y estado de cada participante.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "tournament_participants", 
       uniqueConstraints = @UniqueConstraint(name = "uk_tournament_participant", 
                                           columnNames = {"tournament_id", "user_id"}),
       indexes = {
    @Index(name = "idx_participant_tournament", columnList = "tournament_id"),
    @Index(name = "idx_participant_user", columnList = "user_id"),
    @Index(name = "idx_participant_status", columnList = "status")
})
public class TournamentParticipant extends BaseEntity {

    /**
     * Estados posibles de un participante.
     */
    public enum ParticipantStatus {
        REGISTERED("Registrado"),
        CONFIRMED("Confirmado"),
        CANCELLED("Cancelado"),
        DISQUALIFIED("Descalificado");

        private final String displayName;

        ParticipantStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Torneo al que pertenece este participante.
     */
    @NotNull(message = "El torneo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /**
     * ID del usuario participante.
     */
    @NotNull(message = "El usuario es obligatorio")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Fecha y hora de registro.
     */
    @NotNull(message = "La fecha de registro es obligatoria")
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    /**
     * Estado del participante.
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ParticipantStatus status = ParticipantStatus.REGISTERED;

    /**
     * Nombre del equipo (opcional, para juegos de equipo).
     */
    @Size(max = 50, message = "El nombre del equipo no puede exceder 50 caracteres")
    @Column(name = "team_name", length = 50)
    private String teamName;

    /**
     * Notas adicionales del participante.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Constructor por defecto para JPA.
     */
    protected TournamentParticipant() {
        super();
    }

    /**
     * Constructor para crear un nuevo participante.
     * 
     * @param tournament el torneo
     * @param userId el ID del usuario
     */
    public TournamentParticipant(Tournament tournament, Long userId) {
        super();
        this.tournament = tournament;
        this.userId = userId;
        this.registrationDate = LocalDateTime.now();
        this.status = ParticipantStatus.REGISTERED;
    }

    /**
     * Constructor completo.
     * 
     * @param tournament el torneo
     * @param userId el ID del usuario
     * @param teamName el nombre del equipo
     * @param notes notas adicionales
     */
    public TournamentParticipant(Tournament tournament, Long userId, String teamName, String notes) {
        this(tournament, userId);
        this.teamName = teamName;
        this.notes = notes;
    }

    /**
     * Confirma la participación del usuario.
     */
    public void confirm() {
        if (status != ParticipantStatus.REGISTERED) {
            throw new IllegalStateException("Solo se pueden confirmar participantes registrados");
        }
        this.status = ParticipantStatus.CONFIRMED;
    }

    /**
     * Cancela la participación del usuario.
     */
    public void cancel() {
        if (status == ParticipantStatus.DISQUALIFIED) {
            throw new IllegalStateException("No se puede cancelar un participante descalificado");
        }
        this.status = ParticipantStatus.CANCELLED;
    }

    /**
     * Descalifica al participante.
     * 
     * @param reason motivo de la descalificación
     */
    public void disqualify(String reason) {
        this.status = ParticipantStatus.DISQUALIFIED;
        this.notes = (notes != null ? notes + "\n" : "") + "DESCALIFICADO: " + reason;
    }

    /**
     * Verifica si el participante está activo en el torneo.
     * 
     * @return true si está registrado o confirmado
     */
    public boolean isActiveParticipant() {
        return status == ParticipantStatus.REGISTERED || status == ParticipantStatus.CONFIRMED;
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

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "TournamentParticipant{" +
                "id=" + getId() +
                ", tournamentId=" + (tournament != null ? tournament.getId() : null) +
                ", userId=" + userId +
                ", status=" + status +
                ", registrationDate=" + registrationDate +
                ", teamName='" + teamName + '\'' +
                '}';
    }
} 