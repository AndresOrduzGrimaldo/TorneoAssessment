package com.esport.torneo.domain.game;

import com.esport.torneo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa un tipo de juego disponible para torneos.
 * 
 * Define las características específicas de cada juego como:
 * - Nombre completo y código identificador
 * - Cantidad de jugadores por partida
 * - Configuraciones específicas del juego
 * 
 * Ejemplos: "Counter-Strike 2", "League of Legends", "Valorant", etc.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "games", indexes = {
    @Index(name = "idx_game_code", columnList = "code", unique = true),
    @Index(name = "idx_game_active", columnList = "active"),
    @Index(name = "idx_game_player_count", columnList = "player_count")
})
public class Game extends BaseEntity {

    /**
     * Código único del juego.
     * Usado para identificación programática.
     */
    @NotBlank(message = "El código del juego es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    /**
     * Nombre completo del juego.
     */
    @NotBlank(message = "El nombre completo del juego es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Cantidad de jugadores por partida/equipo.
     */
    @NotNull(message = "La cantidad de jugadores es obligatoria")
    @Positive(message = "La cantidad de jugadores debe ser positiva")
    @Column(name = "player_count", nullable = false)
    private Integer playerCount;

    /**
     * Descripción del juego.
     */
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * URL de la imagen/logo del juego.
     */
    @Size(max = 255, message = "La URL de la imagen no puede exceder 255 caracteres")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Género del juego (FPS, MOBA, Battle Royale, etc.).
     */
    @Size(max = 50, message = "El género no puede exceder 50 caracteres")
    @Column(name = "genre", length = 50)
    private String genre;

    /**
     * Plataforma principal del juego (PC, Console, Mobile).
     */
    @Size(max = 30, message = "La plataforma no puede exceder 30 caracteres")
    @Column(name = "platform", length = 30)
    private String platform;

    /**
     * Constructor por defecto requerido por JPA.
     */
    protected Game() {
        super();
    }

    /**
     * Constructor para crear un nuevo juego.
     * 
     * @param code código único del juego
     * @param fullName nombre completo del juego
     * @param playerCount cantidad de jugadores por partida
     */
    public Game(String code, String fullName, Integer playerCount) {
        super();
        this.code = code;
        this.fullName = fullName;
        this.playerCount = playerCount;
    }

    /**
     * Constructor completo para crear un nuevo juego.
     * 
     * @param code código único del juego
     * @param fullName nombre completo del juego
     * @param playerCount cantidad de jugadores por partida
     * @param description descripción del juego
     * @param imageUrl URL de la imagen del juego
     * @param genre género del juego
     * @param platform plataforma del juego
     */
    public Game(String code, String fullName, Integer playerCount, 
                String description, String imageUrl, String genre, String platform) {
        this(code, fullName, playerCount);
        this.description = description;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.platform = platform;
    }

    /**
     * Obtiene el código del juego.
     * 
     * @return el código único del juego
     */
    public String getCode() {
        return code;
    }

    /**
     * Establece el código del juego.
     * 
     * @param code el código único a establecer
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Obtiene el nombre completo del juego.
     * 
     * @return el nombre completo del juego
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Establece el nombre completo del juego.
     * 
     * @param fullName el nombre completo a establecer
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Obtiene la cantidad de jugadores por partida.
     * 
     * @return la cantidad de jugadores
     */
    public Integer getPlayerCount() {
        return playerCount;
    }

    /**
     * Establece la cantidad de jugadores por partida.
     * 
     * @param playerCount la cantidad de jugadores a establecer
     */
    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    /**
     * Obtiene la descripción del juego.
     * 
     * @return la descripción del juego
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción del juego.
     * 
     * @param description la descripción a establecer
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene la URL de la imagen del juego.
     * 
     * @return la URL de la imagen
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Establece la URL de la imagen del juego.
     * 
     * @param imageUrl la URL de la imagen a establecer
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Obtiene el género del juego.
     * 
     * @return el género del juego
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Establece el género del juego.
     * 
     * @param genre el género a establecer
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Obtiene la plataforma del juego.
     * 
     * @return la plataforma del juego
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Establece la plataforma del juego.
     * 
     * @param platform la plataforma a establecer
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Actualiza la información del juego.
     * 
     * @param fullName nuevo nombre completo
     * @param playerCount nueva cantidad de jugadores
     * @param description nueva descripción
     * @param imageUrl nueva URL de imagen
     * @param genre nuevo género
     * @param platform nueva plataforma
     */
    public void updateGame(String fullName, Integer playerCount, String description,
                          String imageUrl, String genre, String platform) {
        this.fullName = fullName;
        this.playerCount = playerCount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.platform = platform;
    }

    /**
     * Verifica si el juego es multijugador.
     * 
     * @return true si el juego requiere más de un jugador
     */
    public boolean isMultiplayer() {
        return playerCount != null && playerCount > 1;
    }

    /**
     * Verifica si el juego es de equipo.
     * 
     * @return true si el juego requiere más de 2 jugadores
     */
    public boolean isTeamGame() {
        return playerCount != null && playerCount > 2;
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