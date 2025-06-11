package com.esport.torneo.application.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para representar una categoría de torneo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de una categoría de torneo")
public class CategoryDto {

    @Schema(description = "ID único de la categoría", example = "1")
    private Long id;

    @Schema(description = "Nombre de la categoría", example = "FPS", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Schema(description = "Descripción de la categoría", example = "Juegos de disparos en primera persona")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @Schema(description = "Indica si la categoría está activa", example = "true")
    private Boolean active;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto.
     */
    public CategoryDto() {
    }

    /**
     * Constructor completo.
     *
     * @param id ID de la categoría
     * @param name Nombre de la categoría
     * @param description Descripción de la categoría
     * @param active Estado activo/inactivo
     * @param createdAt Fecha de creación
     * @param updatedAt Fecha de actualización
     */
    public CategoryDto(Long id, String name, String description, Boolean active, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    @Override
    public String toString() {
        return "CategoryDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 