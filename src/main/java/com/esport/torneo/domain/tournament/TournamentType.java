package com.esport.torneo.domain.tournament;

/**
 * Enumeración que define los tipos de torneo según su modelo de negocio.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
public enum TournamentType {
    
    /**
     * Torneo gratuito.
     * No requiere pago para participar.
     * Limitado a 2 por organizador.
     */
    FREE("Gratuito"),
    
    /**
     * Torneo de pago.
     * Requiere compra de ticket para participar.
     * Sin límites por organizador.
     */
    PAID("De Pago");

    private final String displayName;

    /**
     * Constructor del enum.
     * 
     * @param displayName nombre para mostrar en la UI
     */
    TournamentType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre para mostrar en la interfaz de usuario.
     * 
     * @return el nombre legible del tipo
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si el torneo es gratuito.
     * 
     * @return true si el torneo es gratuito
     */
    public boolean isFree() {
        return this == FREE;
    }

    /**
     * Verifica si el torneo es de pago.
     * 
     * @return true si el torneo requiere pago
     */
    public boolean isPaid() {
        return this == PAID;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 