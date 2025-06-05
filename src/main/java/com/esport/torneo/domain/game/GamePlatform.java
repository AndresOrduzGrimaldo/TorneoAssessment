package com.esport.torneo.domain.game;

/**
 * Enumeration representing different gaming platforms.
 * Used to categorize games by the platform they run on.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
public enum GamePlatform {
    
    /**
     * Personal Computer (Windows, Mac, Linux)
     */
    PC("PC", "Computadora Personal"),
    
    /**
     * PlayStation console family
     */
    PLAYSTATION("PlayStation", "PlayStation"),
    
    /**
     * Xbox console family
     */
    XBOX("Xbox", "Xbox"),
    
    /**
     * Nintendo console family
     */
    NINTENDO("Nintendo", "Nintendo"),
    
    /**
     * Mobile devices (iOS, Android)
     */
    MOBILE("Mobile", "Móvil"),
    
    /**
     * Web browser games
     */
    WEB("Web Browser", "Navegador Web"),
    
    /**
     * Virtual Reality platforms
     */
    VR("Virtual Reality", "Realidad Virtual"),
    
    /**
     * Cross-platform games (available on multiple platforms)
     */
    CROSS_PLATFORM("Cross Platform", "Multiplataforma"),
    
    /**
     * Arcade machines
     */
    ARCADE("Arcade", "Arcade"),
    
    /**
     * Handheld gaming devices
     */
    HANDHELD("Handheld", "Portátil"),
    
    /**
     * Cloud gaming services
     */
    CLOUD("Cloud Gaming", "Juegos en la Nube"),
    
    /**
     * Other/miscellaneous platforms
     */
    OTHER("Other", "Otros");

    private final String displayName;
    private final String displayNameEs;

    /**
     * Constructor for GamePlatform enum.
     * 
     * @param displayName English display name
     * @param displayNameEs Spanish display name
     */
    GamePlatform(String displayName, String displayNameEs) {
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
     * Converts string to GamePlatform enum (case insensitive).
     * 
     * @param value string value
     * @return GamePlatform enum or null if not found
     */
    public static GamePlatform fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return GamePlatform.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (GamePlatform platform : GamePlatform.values()) {
                if (platform.displayName.equalsIgnoreCase(value) || 
                    platform.displayNameEs.equalsIgnoreCase(value)) {
                    return platform;
                }
            }
            return null;
        }
    }

    /**
     * Checks if the platform supports multiplayer games.
     * 
     * @return true if platform typically supports multiplayer
     */
    public boolean supportsMultiplayer() {
        return switch (this) {
            case PC, PLAYSTATION, XBOX, CROSS_PLATFORM, CLOUD -> true;
            case MOBILE, WEB -> true; // Most modern mobile/web games support multiplayer
            case VR -> true; // VR platforms increasingly support multiplayer
            case NINTENDO -> true; // Nintendo platforms support multiplayer
            case ARCADE, HANDHELD -> false; // Typically single player or local multiplayer only
            case OTHER -> false; // Unknown, assume no multiplayer
        };
    }

    /**
     * Checks if the platform is primarily for competitive gaming.
     * 
     * @return true if platform is commonly used for esports
     */
    public boolean isCompetitivePlatform() {
        return switch (this) {
            case PC, CROSS_PLATFORM -> true; // Primary esports platforms
            case PLAYSTATION, XBOX -> true; // Console esports
            case MOBILE -> true; // Mobile esports growing
            case WEB, CLOUD -> false; // Less common for competitive play
            case VR, NINTENDO, ARCADE, HANDHELD, OTHER -> false;
        };
    }
} 