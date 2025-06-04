package com.esport.torneo.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de juegos.
 * 
 * Utilizado en las operaciones de API REST para
 * enviar y recibir información de videojuegos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDto {

    private Long id;

    @NotBlank(message = "El código del juego es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String fullName;

    @NotNull(message = "El número de jugadores es obligatorio")
    @Positive(message = "El número de jugadores debe ser positivo")
    private Integer playerCount;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @Size(max = 255, message = "La URL de imagen no puede exceder 255 caracteres")
    private String imageUrl;

    @Size(max = 50, message = "El género no puede exceder 50 caracteres")
    private String genre;

    @Size(max = 30, message = "La plataforma no puede exceder 30 caracteres")
    private String platform;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isTeamGame;
    private Boolean hasImage;
    private Boolean isFullyConfigured;

    /**
     * Constructor por defecto.
     */
    public GameDto() {
    }

    /**
     * Constructor para crear DTO básico.
     * 
     * @param code código del juego
     * @param fullName nombre completo
     * @param playerCount número de jugadores
     */
    public GameDto(String code, String fullName, Integer playerCount) {
        this.code = code;
        this.fullName = fullName;
        this.playerCount = playerCount;
    }

    /**
     * Constructor completo.
     * 
     * @param id ID del juego
     * @param code código del juego
     * @param fullName nombre completo
     * @param playerCount número de jugadores
     * @param description descripción
     * @param imageUrl URL de imagen
     * @param genre género del juego
     * @param platform plataforma
     * @param active estado activo
     * @param createdAt fecha de creación
     * @param updatedAt fecha de actualización
     */
    public GameDto(Long id, String code, String fullName, Integer playerCount, 
                   String description, String imageUrl, String genre, String platform,
                   Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.playerCount = playerCount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.platform = platform;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Calcular campos derivados
        this.isTeamGame = playerCount != null && playerCount > 1;
        this.hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
        this.isFullyConfigured = isGameFullyConfigured();
    }

    /**
     * Verifica si el juego está completamente configurado.
     * 
     * @return true si tiene toda la información básica
     */
    private boolean isGameFullyConfigured() {
        return code != null && !code.trim().isEmpty() &&
               fullName != null && !fullName.trim().isEmpty() &&
               playerCount != null && playerCount > 0 &&
               genre != null && !genre.trim().isEmpty() &&
               platform != null && !platform.trim().isEmpty();
    }

    // ======================================================================
    // GETTERS Y SETTERS
    // ======================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.isTeamGame = playerCount != null && playerCount > 1;
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
        this.hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsTeamGame() {
        return isTeamGame;
    }

    public void setIsTeamGame(Boolean isTeamGame) {
        this.isTeamGame = isTeamGame;
    }

    public Boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }

    public Boolean getIsFullyConfigured() {
        return isFullyConfigured;
    }

    public void setIsFullyConfigured(Boolean isFullyConfigured) {
        this.isFullyConfigured = isFullyConfigured;
    }

    @Override
    public String toString() {
        return "GameDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", fullName='" + fullName + '\'' +
                ", playerCount=" + playerCount +
                ", genre='" + genre + '\'' +
                ", platform='" + platform + '\'' +
                ", active=" + active +
                '}';
    }
} 