package com.esport.torneo.domain.ticket;

/**
 * Enumeration representing different ticket states.
 * Defines the lifecycle states of a tournament ticket.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
public enum TicketStatus {
    
    /**
     * Ticket has been reserved but not yet paid
     */
    RESERVED("Reserved", "Reservado", "Ticket reserved, awaiting payment"),
    
    /**
     * Ticket has been paid for and is valid for use
     */
    PAID("Paid", "Pagado", "Ticket paid and ready for use"),
    
    /**
     * Ticket has been used to access the tournament
     */
    USED("Used", "Usado", "Ticket has been used for tournament access"),
    
    /**
     * Ticket has expired and is no longer valid
     */
    EXPIRED("Expired", "Expirado", "Ticket has expired and cannot be used"),
    
    /**
     * Ticket has been cancelled by user or system
     */
    CANCELLED("Cancelled", "Cancelado", "Ticket has been cancelled"),
    
    /**
     * Ticket is pending processing (initial state)
     */
    PENDING("Pending", "Pendiente", "Ticket is being processed"),
    
    /**
     * Ticket has been refunded
     */
    REFUNDED("Refunded", "Reembolsado", "Ticket has been refunded");

    private final String displayName;
    private final String displayNameEs;
    private final String description;

    /**
     * Constructor for TicketStatus enum.
     * 
     * @param displayName English display name
     * @param displayNameEs Spanish display name
     * @param description status description
     */
    TicketStatus(String displayName, String displayNameEs, String description) {
        this.displayName = displayName;
        this.displayNameEs = displayNameEs;
        this.description = description;
    }

    /**
     * Gets the English display name.
     * 
     * @return the English display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the Spanish display name.
     * 
     * @return the Spanish display name
     */
    public String getDisplayNameEs() {
        return displayNameEs;
    }

    /**
     * Gets the status description.
     * 
     * @return the status description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets display name by language code.
     * 
     * @param languageCode language code (en, es)
     * @return localized display name
     */
    public String getDisplayName(String languageCode) {
        if ("es".equalsIgnoreCase(languageCode)) {
            return displayNameEs;
        }
        return displayName;
    }

    /**
     * Converts string to TicketStatus enum (case insensitive).
     * 
     * @param value string value
     * @return TicketStatus enum or null if not found
     */
    public static TicketStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return TicketStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (TicketStatus status : TicketStatus.values()) {
                if (status.displayName.equalsIgnoreCase(value) || 
                    status.displayNameEs.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * Checks if the ticket status is active (can be used).
     * 
     * @return true if ticket is in an active state
     */
    public boolean isActive() {
        return this == PAID || this == RESERVED;
    }

    /**
     * Checks if the ticket status is final (cannot be changed).
     * 
     * @return true if ticket is in a final state
     */
    public boolean isFinal() {
        return this == USED || this == EXPIRED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Checks if the ticket can be cancelled.
     * 
     * @return true if ticket can be cancelled
     */
    public boolean canBeCancelled() {
        return this == RESERVED || this == PAID;
    }

    /**
     * Checks if the ticket can be refunded.
     * 
     * @return true if ticket can be refunded
     */
    public boolean canBeRefunded() {
        return this == PAID;
    }

    /**
     * Checks if the ticket can be used for tournament access.
     * 
     * @return true if ticket can be used
     */
    public boolean canBeUsed() {
        return this == PAID;
    }

    /**
     * Gets the next valid status transitions.
     * 
     * @return array of valid next statuses
     */
    public TicketStatus[] getValidTransitions() {
        return switch (this) {
            case PENDING -> new TicketStatus[]{RESERVED, CANCELLED};
            case RESERVED -> new TicketStatus[]{PAID, EXPIRED, CANCELLED};
            case PAID -> new TicketStatus[]{USED, EXPIRED, CANCELLED, REFUNDED};
            case USED, EXPIRED, CANCELLED, REFUNDED -> new TicketStatus[]{}; // Final states
        };
    }

    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus the target status
     * @return true if transition is valid
     */
    public boolean canTransitionTo(TicketStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        TicketStatus[] validTransitions = getValidTransitions();
        for (TicketStatus validStatus : validTransitions) {
            if (validStatus == targetStatus) {
                return true;
            }
        }
        return false;
    }
} 