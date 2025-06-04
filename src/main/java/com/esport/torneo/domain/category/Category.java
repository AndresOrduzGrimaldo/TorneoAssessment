package com.esport.torneo.domain.category;

import com.esport.torneo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa una categoría de torneo.
 * 
 * Las categorías clasifican los torneos por tipo de juego:
 * - FPS (First Person Shooter)
 * - MOBA (Multiplayer Online Battle Arena)
 * - BR (Battle Royale)
 * - RTS (Real Time Strategy)
 * - etc.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_code", columnList = "code", unique = true),
    @Index(name = "idx_category_active", columnList = "active"),
    @Index(name = "idx_category_deleted", columnList = "deleted_at")
})
public class Category extends BaseEntity {

    /**
     * Código único de la categoría.
     */
    @NotBlank(message = "El código de la categoría es obligatorio")
    @Size(min = 2, max = 20, message = "El código debe tener entre 2 y 20 caracteres")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * Descripción de la categoría.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 3, max = 100, message = "La descripción debe tener entre 3 y 100 caracteres")
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    /**
     * Alias o nombre alternativo de la categoría.
     */
    @Size(max = 50, message = "El alias no puede exceder 50 caracteres")
    @Column(name = "alias", length = 50)
    private String alias;

    /**
     * Constructor por defecto para JPA.
     */
    protected Category() {
        super();
    }

    /**
     * Constructor para crear una nueva categoría.
     * 
     * @param code código único de la categoría
     * @param description descripción de la categoría
     */
    public Category(String code, String description) {
        super();
        this.code = code;
        this.description = description;
    }

    /**
     * Constructor completo.
     * 
     * @param code código único de la categoría
     * @param description descripción de la categoría
     * @param alias alias de la categoría
     */
    public Category(String code, String description, String alias) {
        this(code, description);
        this.alias = alias;
    }

    /**
     * Actualiza la información de la categoría.
     * 
     * @param description nueva descripción
     * @param alias nuevo alias
     */
    public void updateInfo(String description, String alias) {
        this.description = description;
        this.alias = alias;
    }

    /**
     * Verifica si la categoría tiene un alias definido.
     * 
     * @return true si tiene alias
     */
    public boolean hasAlias() {
        return alias != null && !alias.trim().isEmpty();
    }

    /**
     * Obtiene el nombre para mostrar (alias si existe, sino descripción).
     * 
     * @return el nombre para mostrar
     */
    public String getDisplayName() {
        return hasAlias() ? alias : description;
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

    @Override
    public String toString() {
        return "Category{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", alias='" + alias + '\'' +
                ", active=" + getActive() +
                '}';
    }
} 