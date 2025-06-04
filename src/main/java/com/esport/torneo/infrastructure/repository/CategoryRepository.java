package com.esport.torneo.infrastructure.repository;

import com.esport.torneo.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Category.
 * 
 * Proporciona operaciones CRUD y consultas personalizadas
 * para gestionar las categorías de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoría activa por ID.
     * 
     * @param id el ID de la categoría
     * @return la categoría si existe y está activa
     */
    Optional<Category> findByIdAndActiveTrue(Long id);

    /**
     * Busca una categoría por su código único.
     * 
     * @param code el código de la categoría
     * @return la categoría si existe
     */
    Optional<Category> findByCode(String code);

    /**
     * Busca una categoría activa por su código.
     * 
     * @param code el código de la categoría
     * @return la categoría si existe y está activa
     */
    Optional<Category> findByCodeAndActiveTrue(String code);

    /**
     * Busca categorías activas ordenadas por descripción.
     * 
     * @return lista de categorías activas
     */
    List<Category> findByActiveTrueOrderByDescription();

    /**
     * Busca categorías por descripción parcial (case insensitive).
     * 
     * @param description texto a buscar en la descripción
     * @return lista de categorías que coinciden
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY c.description")
    List<Category> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    /**
     * Busca categorías por alias parcial (case insensitive).
     * 
     * @param alias texto a buscar en el alias
     * @return lista de categorías que coinciden
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.alias IS NOT NULL AND " +
           "LOWER(c.alias) LIKE LOWER(CONCAT('%', :alias, '%')) " +
           "ORDER BY c.alias")
    List<Category> findByAliasContainingIgnoreCase(@Param("alias") String alias);

    /**
     * Verifica si existe una categoría con el código dado.
     * 
     * @param code el código a verificar
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Verifica si existe una categoría activa con el código dado.
     * 
     * @param code el código a verificar
     * @return true si existe y está activa
     */
    boolean existsByCodeAndActiveTrue(String code);

    /**
     * Cuenta las categorías activas.
     * 
     * @return número de categorías activas
     */
    long countByActiveTrue();

    /**
     * Busca categorías con alias definido.
     * 
     * @return lista de categorías con alias
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.alias IS NOT NULL " +
           "AND c.alias != '' ORDER BY c.alias")
    List<Category> findCategoriesWithAlias();
} 