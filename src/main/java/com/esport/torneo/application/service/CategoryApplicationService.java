package com.esport.torneo.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.application.mapper.CategoryMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.infrastructure.repository.CategoryRepository;

/**
 * Servicio de aplicación para gestionar categorías.
 * 
 * Coordina las operaciones de negocio relacionadas con categorías,
 * actuando como capa de aplicación en la arquitectura DDD.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class CategoryApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryApplicationService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Constructor del servicio.
     * 
     * @param categoryRepository repositorio de categorías
     * @param categoryMapper mapper de categorías
     */
    public CategoryApplicationService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Crea una nueva categoría.
     * 
     * @param categoryDto datos de la categoría a crear
     * @return la categoría creada
     * @throws IllegalArgumentException si el código ya existe
     */
    @CacheEvict(value = {"categories", "activeCategories"}, allEntries = true)
    public CategoryDto createCategory(CategoryDto categoryDto) {
        logger.info("Creando nueva categoría con código: {}", categoryDto.getCode());

        validateUniqueCode(categoryDto.getCode());

        Category category = new Category(
            categoryDto.getCode(),
            categoryDto.getDescription(),
            categoryDto.getAlias()
        );

        Category savedCategory = categoryRepository.save(category);
        logger.info("Categoría creada exitosamente: {}", savedCategory.getId());

        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Obtiene una categoría por ID.
     * 
     * @param id ID de la categoría
     * @return la categoría encontrada
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        logger.debug("Obteniendo categoría por ID: {}", id);

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        return categoryMapper.toDto(category);
    }

    /**
     * Obtiene una categoría por código.
     * 
     * @param code código de la categoría
     * @return la categoría encontrada
     * @throws IllegalArgumentException si no se encuentra
     */
    @Cacheable(value = "categories", key = "#code")
    @Transactional(readOnly = true)
    public CategoryDto getCategoryByCode(String code) {
        logger.debug("Obteniendo categoría por código: {}", code);

        Category category = categoryRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + code));

        return categoryMapper.toDto(category);
    }

    /**
     * Obtiene todas las categorías activas.
     * 
     * @return lista de categorías activas
     */
    @Cacheable(value = "activeCategories")
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllActiveCategories() {
        logger.debug("Obteniendo todas las categorías activas");

        return categoryRepository.findByActiveTrueOrderByDescription()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca categorías por descripción.
     * 
     * @param description descripción a buscar
     * @return lista de categorías que coinciden
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategoriesByDescription(String description) {
        logger.debug("Buscando categorías por descripción: {}", description);

        return categoryRepository.findByDescriptionContainingIgnoreCase(description)
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca categorías por alias.
     * 
     * @param alias alias a buscar
     * @return lista de categorías que coinciden
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategoriesByAlias(String alias) {
        logger.debug("Buscando categorías por alias: {}", alias);

        return categoryRepository.findByAliasContainingIgnoreCase(alias)
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene categorías que tienen alias definido.
     * 
     * @return lista de categorías con alias
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesWithAlias() {
        logger.debug("Obteniendo categorías con alias");

        return categoryRepository.findCategoriesWithAlias()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría a actualizar
     * @param categoryDto datos actualizados
     * @return la categoría actualizada
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"categories", "activeCategories"}, allEntries = true)
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        logger.info("Actualizando categoría: {}", id);

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        // Validar código único si cambió
        if (!category.getCode().equals(categoryDto.getCode())) {
            validateUniqueCode(categoryDto.getCode());
            category.setCode(categoryDto.getCode());
        }

        category.updateInfo(categoryDto.getDescription(), categoryDto.getAlias());

        Category savedCategory = categoryRepository.save(category);
        logger.info("Categoría actualizada exitosamente: {}", savedCategory.getId());

        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Elimina (soft delete) una categoría.
     * 
     * @param id ID de la categoría a eliminar
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"categories", "activeCategories"}, allEntries = true)
    public void deleteCategory(Long id) {
        logger.info("Eliminando categoría: {}", id);

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        category.markAsDeleted();
        categoryRepository.save(category);

        logger.info("Categoría eliminada exitosamente: {}", id);
    }

    /**
     * Reactiva una categoría eliminada.
     * 
     * @param id ID de la categoría a reactivar
     * @return la categoría reactivada
     * @throws IllegalArgumentException si no se encuentra
     */
    @CacheEvict(value = {"categories", "activeCategories"}, allEntries = true)
    public CategoryDto reactivateCategory(Long id) {
        logger.info("Reactivando categoría: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        if (category.getActive()) {
            throw new IllegalStateException("La categoría ya está activa");
        }

        category.restore();
        Category savedCategory = categoryRepository.save(category);

        logger.info("Categoría reactivada exitosamente: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Obtiene estadísticas de categorías.
     * 
     * @return estadísticas generales
     */
    @Transactional(readOnly = true)
    public CategoryStatsDto getCategoryStats() {
        logger.debug("Obteniendo estadísticas de categorías");

        long totalActive = categoryRepository.countByActiveTrue();
        long totalWithAlias = categoryRepository.findCategoriesWithAlias().size();

        return new CategoryStatsDto(totalActive, totalWithAlias);
    }

    /**
     * Verifica si existe una categoría con el código dado.
     * 
     * @param code código a verificar
     * @return true si existe
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return categoryRepository.existsByCodeAndActiveTrue(code);
    }

    /**
     * Valida que el código de la categoría sea único.
     * 
     * @param code código a validar
     * @throws IllegalArgumentException si ya existe
     */
    private void validateUniqueCode(String code) {
        if (categoryRepository.existsByCodeAndActiveTrue(code)) {
            throw new IllegalArgumentException("Ya existe una categoría con el código: " + code);
        }
    }

    /**
     * DTO para estadísticas de categorías.
     */
    public static class CategoryStatsDto {
        private final long totalActiveCategories;
        private final long totalWithAlias;

        public CategoryStatsDto(long totalActiveCategories, long totalWithAlias) {
            this.totalActiveCategories = totalActiveCategories;
            this.totalWithAlias = totalWithAlias;
        }

        public long getTotalActiveCategories() { return totalActiveCategories; }
        public long getTotalWithAlias() { return totalWithAlias; }
    }
} 