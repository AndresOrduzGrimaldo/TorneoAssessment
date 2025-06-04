package com.esport.torneo.domain.game;

import com.esport.torneo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entidad que representa un videojuego disponible para torneos.
 * 
 * Contiene información sobre:
 * - Identificación del juego (código y nombre)
 * - Configuración de jugadores
 * - Metadatos (género, plataforma, descripción)
 * - URL de imagen
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "games", indexes = {
    @Index(name = "idx_game_code", columnList = "code", unique = true),
    @Index(name = "idx_game_active", columnList = "active"),
    @Index(name = "idx_game_player_count", columnList = "player_count"),
    @Index(name = "idx_game_genre", columnList = "genre"),
    @Index(name = "idx_game_deleted", columnList = "deleted_at")
})
public class Game extends BaseEntity {

    /**
     * Código único del juego.
     */
    @NotBlank(message = "El código del juego es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    /**
     * Nombre completo del juego.
     */
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Número de jugadores por equipo/partida.
     */
    @NotNull(message = "El número de jugadores es obligatorio")
    @Positive(message = "El número de jugadores debe ser positivo")
    @Column(name = "player_count", nullable = false)
    private Integer playerCount;

    /**
     * Descripción del juego.
     */
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * URL de la imagen del juego.
     */
    @Size(max = 255, message = "La URL de imagen no puede exceder 255 caracteres")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Género del juego.
     */
    @Size(max = 50, message = "El género no puede exceder 50 caracteres")
    @Column(name = "genre", length = 50)
    private String genre;

    /**
     * Plataforma del juego.
     */
    @Size(max = 30, message = "La plataforma no puede exceder 30 caracteres")
    @Column(name = "platform", length = 30)
    private String platform;

    /**
     * Constructor por defecto para JPA.
     */
    protected Game() {
        super();
    }

    /**
     * Constructor para crear un nuevo juego.
     * 
     * @param code código único del juego
     * @param fullName nombre completo del juego
     * @param playerCount número de jugadores
     */
    public Game(String code, String fullName, Integer playerCount) {
        super();
        this.code = code;
        this.fullName = fullName;
        this.playerCount = playerCount;
    }

    /**
     * Constructor completo.
     * 
     * @param code código único del juego
     * @param fullName nombre completo del juego
     * @param playerCount número de jugadores
     * @param description descripción del juego
     * @param genre género del juego
     * @param platform plataforma del juego
     */
    public Game(String code, String fullName, Integer playerCount, 
                String description, String genre, String platform) {
        this(code, fullName, playerCount);
        this.description = description;
        this.genre = genre;
        this.platform = platform;
    }

    /**
     * Actualiza la información básica del juego.
     * 
     * @param fullName nuevo nombre completo
     * @param playerCount nuevo número de jugadores
     * @param description nueva descripción
     */
    public void updateBasicInfo(String fullName, Integer playerCount, String description) {
        this.fullName = fullName;
        this.playerCount = playerCount;
        this.description = description;
    }

    /**
     * Actualiza los metadatos del juego.
     * 
     * @param genre nuevo género
     * @param platform nueva plataforma
     * @param imageUrl nueva URL de imagen
     */
    public void updateMetadata(String genre, String platform, String imageUrl) {
        this.genre = genre;
        this.platform = platform;
        this.imageUrl = imageUrl;
    }

    /**
     * Verifica si el juego es de equipo.
     * 
     * @return true si requiere más de un jugador
     */
    public boolean isTeamGame() {
        return playerCount > 1;
    }

    /**
     * Verifica si el juego tiene imagen configurada.
     * 
     * @return true si tiene URL de imagen
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * Verifica si el juego está completamente configurado.
     * 
     * @return true si tiene toda la información básica
     */
    public boolean isFullyConfigured() {
        return code != null && !code.trim().isEmpty() &&
               fullName != null && !fullName.trim().isEmpty() &&
               playerCount != null && playerCount > 0 &&
               genre != null && !genre.trim().isEmpty() &&
               platform != null && !platform.trim().isEmpty();
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", fullName='" + fullName + '\'' +
                ", playerCount=" + playerCount +
                ", genre='" + genre + '\'' +
                ", platform='" + platform + '\'' +
                ", active=" + getActive() +
                '}';
    }
} 