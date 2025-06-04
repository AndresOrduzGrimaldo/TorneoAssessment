package com.esport.torneo.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de categorías.
 * 
 * Utilizado en las operaciones de API REST para
 * enviar y recibir información de categorías.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {

    private Long id;

    @NotBlank(message = "El código de la categoría es obligatorio")
    @Size(min = 2, max = 20, message = "El código debe tener entre 2 y 20 caracteres")
    private String code;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 3, max = 100, message = "La descripción debe tener entre 3 y 100 caracteres")
    private String description;

    @Size(max = 50, message = "El alias no puede exceder 50 caracteres")
    private String alias;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String displayName;

    /**
     * Constructor por defecto.
     */
    public CategoryDto() {
    }

    /**
     * Constructor para crear DTO sin ID.
     * 
     * @param code código de la categoría
     * @param description descripción de la categoría
     * @param alias alias de la categoría
     */
    public CategoryDto(String code, String description, String alias) {
        this.code = code;
        this.description = description;
        this.alias = alias;
    }

    /**
     * Constructor completo.
     * 
     * @param id ID de la categoría
     * @param code código de la categoría
     * @param description descripción de la categoría
     * @param alias alias de la categoría
     * @param active estado activo
     * @param createdAt fecha de creación
     * @param updatedAt fecha de actualización
     */
    public CategoryDto(Long id, String code, String description, String alias, 
                      Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.alias = alias;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.displayName = (alias != null && !alias.trim().isEmpty()) ? alias : description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "CategoryDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", alias='" + alias + '\'' +
                ", active=" + active +
                '}';
    }
} 