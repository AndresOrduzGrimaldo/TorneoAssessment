package com.esport.torneo.domain.game;

/**
 * Enumeration representing different game genres.
 * Used to categorize games by their gameplay style and mechanics.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
public enum GameGenre {
    
    /**
     * First-person shooter games
     */
    FPS("First Person Shooter", "Juegos de disparos en primera persona"),
    
    /**
     * Multiplayer Online Battle Arena games
     */
    MOBA("MOBA", "Multiplayer Online Battle Arena"),
    
    /**
     * Real-time strategy games
     */
    RTS("Real Time Strategy", "Estrategia en tiempo real"),
    
    /**
     * Role-playing games
     */
    RPG("Role Playing Game", "Juego de rol"),
    
    /**
     * Sports simulation games
     */
    SPORTS("Sports", "Deportes"),
    
    /**
     * Racing games
     */
    RACING("Racing", "Carreras"),
    
    /**
     * Fighting games
     */
    FIGHTING("Fighting", "Lucha"),
    
    /**
     * Battle Royale games
     */
    BATTLE_ROYALE("Battle Royale", "Battle Royale"),
    
    /**
     * Card games
     */
    CARD_GAME("Card Game", "Juego de cartas"),
    
    /**
     * Puzzle games
     */
    PUZZLE("Puzzle", "Rompecabezas"),
    
    /**
     * Platform games
     */
    PLATFORM("Platform", "Plataformas"),
    
    /**
     * Simulation games
     */
    SIMULATION("Simulation", "Simulación"),
    
    /**
     * Action games
     */
    ACTION("Action", "Acción"),
    
    /**
     * Adventure games
     */
    ADVENTURE("Adventure", "Aventura"),
    
    /**
     * Other/miscellaneous games
     */
    OTHER("Other", "Otros");

    private final String displayName;
    private final String displayNameEs;

    /**
     * Constructor for GameGenre enum.
     * 
     * @param displayName English display name
     * @param displayNameEs Spanish display name
     */
    GameGenre(String displayName, String displayNameEs) {
        this.displayName = displayName;
        this.displayNameEs = displayNameEs;
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
     * Converts string to GameGenre enum (case insensitive).
     * 
     * @param value string value
     * @return GameGenre enum or null if not found
     */
    public static GameGenre fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return GameGenre.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (GameGenre genre : GameGenre.values()) {
                if (genre.displayName.equalsIgnoreCase(value) || 
                    genre.displayNameEs.equalsIgnoreCase(value)) {
                    return genre;
                }
            }
            return null;
        }
    }
} 