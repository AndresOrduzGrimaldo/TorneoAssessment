package com.esport.torneo.domain.common;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad base que proporciona campos comunes para todas las entidades del dominio.
 * 
 * Incluye:
 * - ID único autogenerado
 * - Timestamps de auditoría (creación y modificación)
 * - Soft delete capability
 * - Métodos equals y hashCode basados en ID
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * Identificador único de la entidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Fecha y hora de creación de la entidad.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última modificación de la entidad.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Fecha y hora de eliminación lógica (soft delete).
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Indica si la entidad está activa (no eliminada).
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Constructor por defecto.
     */
    protected BaseEntity() {
    }

    /**
     * Obtiene el ID de la entidad.
     * 
     * @return el ID único de la entidad
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID de la entidad.
     * 
     * @param id el ID único a establecer
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene la fecha de creación.
     * 
     * @return la fecha y hora de creación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha de creación.
     * 
     * @param createdAt la fecha y hora de creación
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtiene la fecha de última modificación.
     * 
     * @return la fecha y hora de última modificación
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Establece la fecha de última modificación.
     * 
     * @param updatedAt la fecha y hora de última modificación
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Obtiene la fecha de eliminación lógica.
     * 
     * @return la fecha y hora de eliminación, null si no está eliminada
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * Establece la fecha de eliminación lógica.
     * 
     * @param deletedAt la fecha y hora de eliminación
     */
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Verifica si la entidad está activa.
     * 
     * @return true si la entidad está activa, false si está eliminada
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Establece el estado activo de la entidad.
     * 
     * @param active true para activar, false para desactivar
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Marca la entidad como eliminada (soft delete).
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    /**
     * Restaura la entidad eliminada.
     */
    public void restore() {
        this.deletedAt = null;
        this.active = true;
    }

    /**
     * Verifica si la entidad está eliminada.
     * 
     * @return true si la entidad está eliminada
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", active=" + active +
                '}';
    }
} 