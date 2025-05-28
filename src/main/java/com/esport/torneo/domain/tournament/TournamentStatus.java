package com.esport.torneo.domain.tournament;

/**
 * Enumeración que define los estados posibles de un torneo.
 * 
 * El ciclo de vida de un torneo sigue esta secuencia:
 * DRAFT -> PUBLISHED -> IN_PROGRESS -> FINISHED
 * 
 * En cualquier momento puede ser CANCELLED.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
public enum TournamentStatus {
    
    /**
     * Torneo en borrador, aún no publicado.
     * Solo visible para el organizador.
     */
    DRAFT("Borrador"),
    
    /**
     * Torneo publicado y abierto para inscripciones.
     * Visible para todos los usuarios.
     */
    PUBLISHED("Publicado"),
    
    /**
     * Torneo en progreso, ya iniciado.
     * No se permiten más inscripciones.
     */
    IN_PROGRESS("En Progreso"),
    
    /**
     * Torneo finalizado.
     * Resultados disponibles.
     */
    FINISHED("Finalizado"),
    
    /**
     * Torneo cancelado.
     * No se puede reactivar.
     */
    CANCELLED("Cancelado");

    private final String displayName;

    /**
     * Constructor del enum.
     * 
     * @param displayName nombre para mostrar en la UI
     */
    TournamentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre para mostrar en la interfaz de usuario.
     * 
     * @return el nombre legible del estado
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si el torneo está activo (no cancelado ni finalizado).
     * 
     * @return true si el torneo está activo
     */
    public boolean isActive() {
        return this != CANCELLED && this != FINISHED;
    }

    /**
     * Verifica si el torneo permite inscripciones.
     * 
     * @return true si se pueden realizar inscripciones
     */
    public boolean allowsRegistration() {
        return this == PUBLISHED;
    }

    /**
     * Verifica si el torneo está en progreso.
     * 
     * @return true si el torneo está en curso
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * Verifica si el torneo ha finalizado.
     * 
     * @return true si el torneo ha terminado
     */
    public boolean isFinished() {
        return this == FINISHED;
    }

    /**
     * Verifica si el torneo está cancelado.
     * 
     * @return true si el torneo está cancelado
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Verifica si es posible cambiar al estado especificado.
     * 
     * @param newStatus el nuevo estado deseado
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(TournamentStatus newStatus) {
        if (this == newStatus) {
            return false; // No cambio
        }
        
        switch (this) {
            case DRAFT:
                return newStatus == PUBLISHED || newStatus == CANCELLED;
            case PUBLISHED:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == FINISHED || newStatus == CANCELLED;
            case FINISHED:
            case CANCELLED:
                return false; // Estados finales
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
} 