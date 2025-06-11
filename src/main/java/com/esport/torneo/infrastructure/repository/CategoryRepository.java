package com.esport.torneo.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esport.torneo.domain.category.Category;

/**
 * Repository interface for Category entity operations.
 * Provides data access methods for category management.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by code (case insensitive).
     * 
     * @param code the category code
     * @return Optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE UPPER(c.code) = UPPER(:code) AND c.deletedAt IS NULL")
    Optional<Category> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Find all active categories (not soft deleted).
     * 
     * @return List of active categories
     */
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL ORDER BY c.description")
    List<Category> findAllActive();

    /**
     * Find active categories with pagination.
     * 
     * @param pageable pagination information
     * @return Page of active categories
     */
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    Page<Category> findAllActive(Pageable pageable);

    /**
     * Search categories by description containing text (case insensitive).
     * 
     * @param description text to search in description
     * @param pageable pagination information
     * @return Page of matching categories
     */
    @Query("SELECT c FROM Category c WHERE UPPER(c.description) LIKE UPPER(CONCAT('%', :description, '%')) AND c.deletedAt IS NULL")
    Page<Category> findByDescriptionContainingIgnoreCase(@Param("description") String description, Pageable pageable);

    /**
     * Check if category code exists (excluding specific ID).
     * 
     * @param code the category code
     * @param excludeId ID to exclude from search
     * @return true if code exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE UPPER(c.code) = UPPER(:code) AND c.id != :excludeId AND c.deletedAt IS NULL")
    boolean existsByCodeIgnoreCaseAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * Check if category code exists.
     * 
     * @param code the category code
     * @return true if code exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE UPPER(c.code) = UPPER(:code) AND c.deletedAt IS NULL")
    boolean existsByCodeIgnoreCase(@Param("code") String code);

    /**
     * Count active categories.
     * 
     * @return number of active categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.deletedAt IS NULL")
    long countActive();

    /**
     * Busca una categoría por nombre (ignorando mayúsculas/minúsculas).
     *
     * @param name el nombre a buscar
     * @return la categoría encontrada o empty
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Busca categorías activas.
     *
     * @return lista de categorías activas
     */
    List<Category> findByActiveTrue();

    /**
     * Busca categorías activas con paginación.
     *
     * @param pageable información de paginación
     * @return página de categorías activas
     */
    Page<Category> findByActiveTrue(Pageable pageable);

    /**
     * Busca categorías por nombre que contenga el texto (ignorando mayúsculas).
     *
     * @param name el texto a buscar en el nombre
     * @param pageable información de paginación
     * @return página de categorías que coinciden
     */
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Busca categorías activas por nombre que contenga el texto.
     *
     * @param name el texto a buscar
     * @param pageable información de paginación
     * @return página de categorías activas que coinciden
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findActiveByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Verifica si existe una categoría con el nombre dado (ignorando mayúsculas).
     *
     * @param name el nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica si existe otra categoría con el mismo nombre (para updates).
     *
     * @param name el nombre a verificar
     * @param id el ID de la categoría a excluir
     * @return true si existe otra categoría con ese nombre
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Cuenta las categorías activas.
     *
     * @return número de categorías activas
     */
    long countByActiveTrue();

    /**
     * Busca categorías sin borrado lógico.
     *
     * @return lista de categorías no eliminadas
     */
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAllNotDeleted();

    /**
     * Busca categorías activas sin borrado lógico.
     *
     * @return lista de categorías activas no eliminadas
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.deletedAt IS NULL")
    List<Category> findActiveNotDeleted();
} 