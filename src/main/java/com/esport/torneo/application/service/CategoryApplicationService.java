package com.esport.torneo.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.application.mapper.CategoryMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.infrastructure.repository.CategoryRepository;

/**
 * Servicio de aplicación para la gestión de categorías.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class CategoryApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryApplicationService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryApplicationService(CategoryRepository categoryRepository, 
                                   CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Crea una nueva categoría.
     *
     * @param categoryDto datos de la categoría
     * @return la categoría creada
     * @throws IllegalArgumentException si ya existe una categoría con ese nombre
     */
    public CategoryDto createCategory(CategoryDto categoryDto) {
        logger.info("Creando nueva categoría: {}", categoryDto.getName());

        validateCategoryName(categoryDto.getName(), null);

        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);

        logger.info("Categoría creada exitosamente con ID: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Obtiene una categoría por ID.
     *
     * @param id el ID de la categoría
     * @return la categoría encontrada
     * @throws IllegalArgumentException si no se encuentra la categoría
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        logger.debug("Buscando categoría con ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        return categoryMapper.toDto(category);
    }

    /**
     * Obtiene todas las categorías con paginación.
     *
     * @param pageable información de paginación
     * @return página de categorías
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        logger.debug("Obteniendo todas las categorías - página: {}, tamaño: {}", 
                     pageable.getPageNumber(), pageable.getPageSize());

        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    /**
     * Obtiene todas las categorías activas.
     *
     * @return lista de categorías activas
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        logger.debug("Obteniendo categorías activas");

        return categoryRepository.findByActiveTrue()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca categorías por nombre.
     *
     * @param name el nombre a buscar
     * @param pageable información de paginación
     * @return página de categorías que coinciden
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategoriesByName(String name, Pageable pageable) {
        logger.debug("Buscando categorías por nombre: {}", name);

        return categoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(categoryMapper::toDto);
    }

    /**
     * Busca categorías activas por nombre.
     *
     * @param name el nombre a buscar
     * @param pageable información de paginación
     * @return página de categorías activas que coinciden
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchActiveCategoriesByName(String name, Pageable pageable) {
        logger.debug("Buscando categorías activas por nombre: {}", name);

        return categoryRepository.findActiveByNameContaining(name, pageable)
                .map(categoryMapper::toDto);
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param id el ID de la categoría
     * @param categoryDto los nuevos datos
     * @return la categoría actualizada
     * @throws IllegalArgumentException si no se encuentra la categoría o el nombre ya existe
     */
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        logger.info("Actualizando categoría con ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        if (categoryDto.getName() != null && !categoryDto.getName().equals(existingCategory.getName())) {
            validateCategoryName(categoryDto.getName(), id);
        }

        categoryMapper.updateEntityFromDto(existingCategory, categoryDto);
        Category updatedCategory = categoryRepository.save(existingCategory);

        logger.info("Categoría actualizada exitosamente con ID: {}", id);
        return categoryMapper.toDto(updatedCategory);
    }

    /**
     * Activa una categoría.
     *
     * @param id el ID de la categoría
     * @return la categoría activada
     */
    public CategoryDto activateCategory(Long id) {
        logger.info("Activando categoría con ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        category.activate();
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Desactiva una categoría.
     *
     * @param id el ID de la categoría
     * @return la categoría desactivada
     */
    public CategoryDto deactivateCategory(Long id) {
        logger.info("Desactivando categoría con ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        category.deactivate();
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Elimina una categoría (soft delete).
     *
     * @param id el ID de la categoría
     */
    public void deleteCategory(Long id) {
        logger.info("Eliminando categoría con ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        // Verificar si la categoría está siendo usada en torneos
        // TODO: Implementar validación de uso en torneos
        
        category.delete();
        categoryRepository.save(category);

        logger.info("Categoría eliminada exitosamente con ID: {}", id);
    }

    /**
     * Valida que el nombre de la categoría sea único.
     *
     * @param name el nombre a validar
     * @param excludeId ID a excluir de la validación (para updates)
     * @throws IllegalArgumentException si ya existe una categoría con ese nombre
     */
    private void validateCategoryName(String name, Long excludeId) {
        boolean exists = excludeId != null 
            ? categoryRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
            : categoryRepository.existsByNameIgnoreCase(name);

        if (exists) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + name);
        }
    }

    /**
     * Obtiene el conteo de categorías activas.
     *
     * @return número de categorías activas
     */
    @Transactional(readOnly = true)
    public long getActiveCategoriesCount() {
        return categoryRepository.countByActiveTrue();
    }
} 