package com.esport.torneo.domain.category;

import com.esport.torneo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa una categoría de torneo.
 * 
 * Las categorías permiten clasificar los torneos por tipo de juego,
 * modalidad o cualquier otra característica relevante.
 * 
 * Ejemplos: "FPS", "MOBA", "Battle Royale", "Racing", etc.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_code", columnList = "code", unique = true),
    @Index(name = "idx_category_active", columnList = "active")
})
public class Category extends BaseEntity {

    /**
     * Código único de la categoría.
     * Usado para identificación programática.
     */
    @NotBlank(message = "El código de la categoría es obligatorio")
    @Size(min = 2, max = 20, message = "El código debe tener entre 2 y 20 caracteres")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * Descripción completa de la categoría.
     */
    @NotBlank(message = "La descripción de la categoría es obligatoria")
    @Size(min = 3, max = 100, message = "La descripción debe tener entre 3 y 100 caracteres")
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    /**
     * Alias o nombre corto de la categoría.
     * Usado para mostrar en interfaces de usuario.
     */
    @Size(max = 50, message = "El alias no puede exceder 50 caracteres")
    @Column(name = "alias", length = 50)
    private String alias;

    /**
     * Constructor por defecto requerido por JPA.
     */
    protected Category() {
        super();
    }

    /**
     * Constructor para crear una nueva categoría.
     * 
     * @param code código único de la categoría
     * @param description descripción de la categoría
     * @param alias alias o nombre corto
     */
    public Category(String code, String description, String alias) {
        super();
        this.code = code;
        this.description = description;
        this.alias = alias;
    }

    /**
     * Constructor para crear una categoría sin alias.
     * 
     * @param code código único de la categoría
     * @param description descripción de la categoría
     */
    public Category(String code, String description) {
        this(code, description, null);
    }

    /**
     * Obtiene el código de la categoría.
     * 
     * @return el código único de la categoría
     */
    public String getCode() {
        return code;
    }

    /**
     * Establece el código de la categoría.
     * 
     * @param code el código único a establecer
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Obtiene la descripción de la categoría.
     * 
     * @return la descripción de la categoría
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción de la categoría.
     * 
     * @param description la descripción a establecer
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene el alias de la categoría.
     * 
     * @return el alias de la categoría
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Establece el alias de la categoría.
     * 
     * @param alias el alias a establecer
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Actualiza los datos de la categoría.
     * 
     * @param description nueva descripción
     * @param alias nuevo alias
     */
    public void updateCategory(String description, String alias) {
        this.description = description;
        this.alias = alias;
    }

    /**
     * Obtiene el nombre para mostrar (alias si existe, sino descripción).
     * 
     * @return el nombre a mostrar en la UI
     */
    public String getDisplayName() {
        return alias != null && !alias.trim().isEmpty() ? alias : description;
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