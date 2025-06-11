package com.esport.torneo.application.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para representar un juego/videojuego.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de un juego/videojuego")
public class GameDto {

    @Schema(description = "ID único del juego", example = "1")
    private Long id;

    @Schema(description = "Nombre del juego", example = "Counter-Strike 2", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Schema(description = "Descripción del juego", example = "Juego de disparos táctico en primera persona")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @Schema(description = "Mínimo número de jugadores", example = "2", required = true)
    @NotNull(message = "El mínimo de jugadores es obligatorio")
    @Positive(message = "El mínimo de jugadores debe ser positivo")
    private Integer minPlayers;

    @Schema(description = "Máximo número de jugadores", example = "10", required = true)
    @NotNull(message = "El máximo de jugadores es obligatorio")
    @Positive(message = "El máximo de jugadores debe ser positivo")
    private Integer maxPlayers;

    @Schema(description = "Género del juego", example = "FPS")
    @Size(max = 50, message = "El género no puede exceder 50 caracteres")
    private String genre;

    @Schema(description = "Plataforma del juego", example = "PC, PlayStation, Xbox")
    @Size(max = 100, message = "La plataforma no puede exceder 100 caracteres")
    private String platform;

    @Schema(description = "Desarrollador del juego", example = "Valve Corporation")
    @Size(max = 100, message = "El desarrollador no puede exceder 100 caracteres")
    private String developer;

    @Schema(description = "URL de la imagen del juego")
    @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
    private String imageUrl;

    @Schema(description = "Indica si el juego está activo", example = "true")
    private Boolean active;

    @Schema(description = "ID de la categoría asociada", example = "1")
    private Long categoryId;

    @Schema(description = "Nombre de la categoría asociada", example = "FPS")
    private String categoryName;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto.
     */
    public GameDto() {
    }

    /**
     * Constructor completo.
     */
    public GameDto(Long id, String name, String description, Integer minPlayers, Integer maxPlayers,
                   String genre, String platform, String developer, String imageUrl, Boolean active,
                   Long categoryId, String categoryName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.genre = genre;
        this.platform = platform;
        this.developer = developer;
        this.imageUrl = imageUrl;
        this.active = active;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
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

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    @Override
    public String toString() {
        return "GameDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", genre='" + genre + '\'' +
                ", platform='" + platform + '\'' +
                ", developer='" + developer + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", active=" + active +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 