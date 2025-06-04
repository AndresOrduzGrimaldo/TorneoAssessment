package com.esport.torneo.domain.tournament;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.esport.torneo.domain.category.Category;
import com.esport.torneo.domain.common.BaseEntity;
import com.esport.torneo.domain.game.Game;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidad raíz del agregado Tournament.
 * 
 * Representa un torneo de videojuegos con todas sus características:
 * - Información básica (nombre, descripción, fechas)
 * - Configuración (tipo, límites, precios)
 * - Estado y participantes
 * - Reglas y configuración de streaming
 * 
 * Implementa las reglas de negocio del dominio de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "tournaments", indexes = {
    @Index(name = "idx_tournament_status", columnList = "status"),
    @Index(name = "idx_tournament_type", columnList = "tournament_type"),
    @Index(name = "idx_tournament_category", columnList = "category_id"),
    @Index(name = "idx_tournament_game", columnList = "game_id"),
    @Index(name = "idx_tournament_organizer", columnList = "organizer_id"),
    @Index(name = "idx_tournament_start_date", columnList = "start_date"),
    @Index(name = "idx_tournament_active", columnList = "active")
})
public class Tournament extends BaseEntity {

    /**
     * Nombre del torneo.
     */
    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Descripción detallada del torneo.
     */
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Tipo de torneo (FREE o PAID).
     */
    @NotNull(message = "El tipo de torneo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type", nullable = false, length = 10)
    private TournamentType tournamentType = TournamentType.FREE;

    /**
     * Estado actual del torneo.
     */
    @NotNull(message = "El estado del torneo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TournamentStatus status = TournamentStatus.DRAFT;

    /**
     * Categoría del torneo.
     */
    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Juego del torneo.
     */
    @NotNull(message = "El juego es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     * ID del organizador del torneo.
     */
    @NotNull(message = "El organizador es obligatorio")
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    /**
     * Máximo número de participantes.
     */
    @NotNull(message = "El máximo de participantes es obligatorio")
    @Positive(message = "El máximo de participantes debe ser positivo")
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    /**
     * Número actual de participantes registrados.
     */
    @Min(value = 0, message = "Los participantes actuales no pueden ser negativos")
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    /**
     * Precio de entrada (0 para torneos gratuitos).
     */
    @NotNull(message = "El precio de entrada es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de entrada no puede ser negativo")
    @Column(name = "entry_fee", precision = 10, scale = 2)
    private BigDecimal entryFee = BigDecimal.ZERO;

    /**
     * Pozo de premios.
     */
    @NotNull(message = "El pozo de premios es obligatorio")
    @DecimalMin(value = "0.0", message = "El pozo de premios no puede ser negativo")
    @Column(name = "prize_pool", precision = 10, scale = 2)
    private BigDecimal prizePool = BigDecimal.ZERO;

    /**
     * Tasa de comisión de la plataforma.
     */
    @NotNull(message = "La tasa de comisión es obligatoria")
    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @DecimalMax(value = "1.0", message = "La comisión no puede ser mayor al 100%")
    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = new BigDecimal("0.0500"); // 5%

    /**
     * Fecha y hora de inicio del torneo.
     */
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Fecha y hora de finalización del torneo.
     */
    @NotNull(message = "La fecha de finalización es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Fecha y hora de inicio de registraciones.
     */
    @NotNull(message = "La fecha de inicio de registraciones es obligatoria")
    @Column(name = "registration_start", nullable = false)
    private LocalDateTime registrationStart;

    /**
     * Fecha y hora de finalización de registraciones.
     */
    @NotNull(message = "La fecha de finalización de registraciones es obligatoria")
    @Column(name = "registration_end", nullable = false)
    private LocalDateTime registrationEnd;

    /**
     * URL del stream del torneo.
     */
    @Size(max = 255, message = "La URL del stream no puede exceder 255 caracteres")
    @Column(name = "stream_url", length = 255)
    private String streamUrl;

    /**
     * Plataforma de streaming (Twitch, YouTube, Discord, etc.).
     */
    @Size(max = 30, message = "La plataforma de stream no puede exceder 30 caracteres")
    @Column(name = "stream_platform", length = 30)
    private String streamPlatform;

    /**
     * Reglas del torneo.
     */
    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    /**
     * URL de la imagen banner del torneo.
     */
    @Size(max = 255, message = "La URL del banner no puede exceder 255 caracteres")
    @Column(name = "banner_image_url", length = 255)
    private String bannerImageUrl;

    /**
     * Lista de participantes del torneo.
     */
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TournamentParticipant> participants = new ArrayList<>();

    /**
     * Lista de etapas del torneo.
     */
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stageOrder ASC")
    private List<TournamentStage> stages = new ArrayList<>();

    /**
     * Constructor por defecto para JPA.
     */
    protected Tournament() {
        super();
    }

    /**
     * Constructor para crear un nuevo torneo.
     * 
     * @param name nombre del torneo
     * @param description descripción del torneo
     * @param tournamentType tipo de torneo (FREE/PAID)
     * @param category categoría del torneo
     * @param game juego del torneo
     * @param organizerId ID del organizador
     * @param maxParticipants máximo de participantes
     * @param startDate fecha de inicio
     * @param endDate fecha de finalización
     * @param registrationStart inicio de registraciones
     * @param registrationEnd fin de registraciones
     */
    public Tournament(String name, String description, TournamentType tournamentType,
                     Category category, Game game, Long organizerId, Integer maxParticipants,
                     LocalDateTime startDate, LocalDateTime endDate,
                     LocalDateTime registrationStart, LocalDateTime registrationEnd) {
        super();
        this.name = name;
        this.description = description;
        this.tournamentType = tournamentType;
        this.category = category;
        this.game = game;
        this.organizerId = organizerId;
        this.maxParticipants = maxParticipants;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        
        validateDates();
        validateTournamentType();
    }

    // ======================================================================
    // MÉTODOS DE NEGOCIO
    // ======================================================================

    /**
     * Valida las fechas del torneo.
     */
    private void validateDates() {
        if (endDate != null && startDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la de inicio");
        }
        
        if (registrationEnd != null && registrationStart != null && !registrationEnd.isAfter(registrationStart)) {
            throw new IllegalArgumentException("La fecha de fin de registraciones debe ser posterior a la de inicio");
        }
        
        if (registrationEnd != null && startDate != null && registrationEnd.isAfter(startDate)) {
            throw new IllegalArgumentException("Las registraciones deben cerrar antes del inicio del torneo");
        }
    }

    /**
     * Valida la consistencia del tipo de torneo.
     */
    private void validateTournamentType() {
        if (tournamentType == TournamentType.FREE && entryFee.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Los torneos gratuitos no pueden tener precio de entrada");
        }
        
        if (tournamentType == TournamentType.PAID && entryFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los torneos de pago deben tener un precio de entrada mayor a cero");
        }
    }

    /**
     * Publica el torneo cambiando su estado a PUBLISHED.
     * 
     * @throws IllegalStateException si el torneo no está en estado DRAFT
     */
    public void publish() {
        if (!status.canTransitionTo(TournamentStatus.PUBLISHED)) {
            throw new IllegalStateException("No se puede publicar el torneo en su estado actual: " + status);
        }
        
        validateForPublication();
        this.status = TournamentStatus.PUBLISHED;
    }

    /**
     * Valida que el torneo esté listo para publicación.
     */
    private void validateForPublication() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("El torneo debe tener un nombre para ser publicado");
        }
        
        if (category == null) {
            throw new IllegalStateException("El torneo debe tener una categoría para ser publicado");
        }
        
        if (game == null) {
            throw new IllegalStateException("El torneo debe tener un juego para ser publicado");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalStateException("El torneo debe tener fechas definidas para ser publicado");
        }
        
        if (registrationStart == null || registrationEnd == null) {
            throw new IllegalStateException("El torneo debe tener período de registración definido para ser publicado");
        }
    }

    /**
     * Inicia el torneo cambiando su estado a IN_PROGRESS.
     * 
     * @throws IllegalStateException si el torneo no puede iniciarse
     */
    public void start() {
        if (!status.canTransitionTo(TournamentStatus.IN_PROGRESS)) {
            throw new IllegalStateException("No se puede iniciar el torneo en su estado actual: " + status);
        }
        
        if (currentParticipants < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 participantes para iniciar el torneo");
        }
        
        this.status = TournamentStatus.IN_PROGRESS;
    }

    /**
     * Finaliza el torneo cambiando su estado a FINISHED.
     * 
     * @throws IllegalStateException si el torneo no puede finalizarse
     */
    public void finish() {
        if (!status.canTransitionTo(TournamentStatus.FINISHED)) {
            throw new IllegalStateException("No se puede finalizar el torneo en su estado actual: " + status);
        }
        
        this.status = TournamentStatus.FINISHED;
    }

    /**
     * Cancela el torneo cambiando su estado a CANCELLED.
     * 
     * @throws IllegalStateException si el torneo no puede cancelarse
     */
    public void cancel() {
        if (!status.canTransitionTo(TournamentStatus.CANCELLED)) {
            throw new IllegalStateException("No se puede cancelar el torneo en su estado actual: " + status);
        }
        
        this.status = TournamentStatus.CANCELLED;
    }

    /**
     * Registra un nuevo participante en el torneo.
     * 
     * @param participant el participante a registrar
     * @throws IllegalStateException si no se puede registrar
     */
    public void addParticipant(TournamentParticipant participant) {
        if (!status.allowsRegistration()) {
            throw new IllegalStateException("El torneo no permite registraciones en su estado actual");
        }
        
        if (currentParticipants >= maxParticipants) {
            throw new IllegalStateException("El torneo ha alcanzado el máximo de participantes");
        }
        
        if (!isRegistrationOpen()) {
            throw new IllegalStateException("El período de registración no está abierto");
        }
        
        participants.add(participant);
        currentParticipants++;
    }

    /**
     * Método de conveniencia para agregar un participante con userId y teamName.
     * 
     * @param userId ID del usuario
     * @param teamName nombre del equipo (opcional)
     */
    public void addParticipant(Long userId, String teamName) {
        TournamentParticipant participant = new TournamentParticipant(this, userId, teamName, null);
        addParticipant(participant);
    }

    /**
     * Verifica si el torneo permite registrar nuevos participantes.
     * 
     * @return true si se pueden registrar participantes
     */
    public boolean canRegisterParticipants() {
        return status.allowsRegistration() && 
               isRegistrationOpen() && 
               hasAvailableSlots();
    }

    /**
     * Verifica si el período de registración está abierto.
     * 
     * @return true si las registraciones están abiertas
     */
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(registrationStart) && now.isBefore(registrationEnd);
    }

    /**
     * Verifica si el torneo tiene cupos disponibles.
     * 
     * @return true si hay cupos disponibles
     */
    public boolean hasAvailableSlots() {
        return currentParticipants < maxParticipants;
    }

    /**
     * Calcula la comisión total generada por el torneo.
     * 
     * @return la comisión total
     */
    public BigDecimal calculateTotalCommission() {
        if (tournamentType == TournamentType.FREE) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalRevenue = entryFee.multiply(new BigDecimal(currentParticipants));
        return totalRevenue.multiply(commissionRate);
    }

    /**
     * Actualiza la información básica del torneo.
     * 
     * @param name nuevo nombre
     * @param description nueva descripción
     * @param maxParticipants nuevo máximo de participantes
     * @param entryFee nuevo precio de entrada
     * @param prizePool nuevo pozo de premios
     */
    public void updateBasicInfo(String name, String description, Integer maxParticipants,
                               BigDecimal entryFee, BigDecimal prizePool) {
        if (status != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Solo se puede editar la información en estado borrador");
        }
        
        this.name = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.entryFee = entryFee;
        this.prizePool = prizePool;
        
        validateTournamentType();
    }

    /**
     * Actualiza las fechas del torneo.
     * 
     * @param startDate nueva fecha de inicio
     * @param endDate nueva fecha de fin
     * @param registrationStart nuevo inicio de registraciones
     * @param registrationEnd nuevo fin de registraciones
     */
    public void updateDates(LocalDateTime startDate, LocalDateTime endDate,
                           LocalDateTime registrationStart, LocalDateTime registrationEnd) {
        if (status != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden editar las fechas en estado borrador");
        }
        
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        
        validateDates();
    }

    /**
     * Configura el streaming del torneo.
     * 
     * @param streamUrl URL del stream
     * @param streamPlatform plataforma de streaming
     */
    public void configureStreaming(String streamUrl, String streamPlatform) {
        this.streamUrl = streamUrl;
        this.streamPlatform = streamPlatform;
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

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
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

    public List<TournamentParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<TournamentParticipant> participants) {
        this.participants = participants;
    }

    public List<TournamentStage> getStages() {
        return stages;
    }

    public void setStages(List<TournamentStage> stages) {
        this.stages = stages;
    }

    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", tournamentType=" + tournamentType +
                ", status=" + status +
                ", maxParticipants=" + maxParticipants +
                ", currentParticipants=" + currentParticipants +
                ", entryFee=" + entryFee +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
} 