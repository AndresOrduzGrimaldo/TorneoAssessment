package com.esport.torneo.application.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.application.mapper.CategoryMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.infrastructure.repository.CategoryRepository;

/**
 * Tests unitarios para CategoryApplicationService.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Category Application Service Tests")
class CategoryApplicationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryApplicationService categoryService;

    private Category sampleCategory;
    private CategoryDto sampleCategoryDto;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        sampleCategory = new Category("FPS", "First Person Shooter games");
        sampleCategory.setId(1L);
        sampleCategory.setActive(true);
        sampleCategory.setCreatedAt(LocalDateTime.now());
        sampleCategory.setUpdatedAt(LocalDateTime.now());

        sampleCategoryDto = new CategoryDto(
            1L, "FPS", "First Person Shooter games", true,
            LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Debe crear una categoría exitosamente")
    void shouldCreateCategorySuccessfully() {
        // Given
        CategoryDto newCategoryDto = new CategoryDto();
        newCategoryDto.setName("RTS");
        newCategoryDto.setDescription("Real Time Strategy games");

        Category categoryToSave = new Category("RTS", "Real Time Strategy games");
        Category savedCategory = new Category("RTS", "Real Time Strategy games");
        savedCategory.setId(2L);

        when(categoryRepository.existsByNameIgnoreCase("RTS")).thenReturn(false);
        when(categoryMapper.toEntity(newCategoryDto)).thenReturn(categoryToSave);
        when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);
        when(categoryMapper.toDto(savedCategory)).thenReturn(sampleCategoryDto);

        // When
        CategoryDto result = categoryService.createCategory(newCategoryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("FPS");
        verify(categoryRepository).existsByNameIgnoreCase("RTS");
        verify(categoryRepository).save(categoryToSave);
        verify(categoryMapper).toDto(savedCategory);
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear categoría con nombre duplicado")
    void shouldThrowExceptionWhenCreatingCategoryWithDuplicateName() {
        // Given
        CategoryDto duplicateCategoryDto = new CategoryDto();
        duplicateCategoryDto.setName("FPS");
        duplicateCategoryDto.setDescription("Duplicate category");

        when(categoryRepository.existsByNameIgnoreCase("FPS")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(duplicateCategoryDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe una categoría con el nombre: FPS");

        verify(categoryRepository).existsByNameIgnoreCase("FPS");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener categoría por ID exitosamente")
    void shouldGetCategoryByIdSuccessfully() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        // When
        CategoryDto result = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("FPS");
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toDto(sampleCategory);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener categoría inexistente")
    void shouldThrowExceptionWhenGettingNonExistentCategory() {
        // Given
        Long nonExistentId = 999L;
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(nonExistentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Categoría no encontrada con ID: 999");

        verify(categoryRepository).findById(nonExistentId);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Debe obtener categorías activas exitosamente")
    void shouldGetActiveCategoriesSuccessfully() {
        // Given
        Category activeCategory1 = new Category("FPS", "First Person Shooter");
        Category activeCategory2 = new Category("RPG", "Role Playing Games");
        List<Category> activeCategories = Arrays.asList(activeCategory1, activeCategory2);

        CategoryDto activeCategoryDto1 = new CategoryDto(1L, "FPS", "First Person Shooter", true, LocalDateTime.now(), LocalDateTime.now());
        CategoryDto activeCategoryDto2 = new CategoryDto(2L, "RPG", "Role Playing Games", true, LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findByActiveTrue()).thenReturn(activeCategories);
        when(categoryMapper.toDto(activeCategory1)).thenReturn(activeCategoryDto1);
        when(categoryMapper.toDto(activeCategory2)).thenReturn(activeCategoryDto2);

        // When
        List<CategoryDto> result = categoryService.getActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("FPS");
        assertThat(result.get(1).getName()).isEqualTo("RPG");
        verify(categoryRepository).findByActiveTrue();
        verify(categoryMapper, times(2)).toDto(any(Category.class));
    }

    @Test
    @DisplayName("Debe activar categoría exitosamente")
    void shouldActivateCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        Category inactiveCategory = new Category("FPS", "First Person Shooter");
        inactiveCategory.setId(categoryId);
        inactiveCategory.setActive(false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactiveCategory));
        when(categoryRepository.save(inactiveCategory)).thenReturn(inactiveCategory);
        when(categoryMapper.toDto(inactiveCategory)).thenReturn(sampleCategoryDto);

        // When
        CategoryDto result = categoryService.activateCategory(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(inactiveCategory.getActive()).isTrue();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(inactiveCategory);
        verify(categoryMapper).toDto(inactiveCategory);
    }

    @Test
    @DisplayName("Debe desactivar categoría exitosamente")
    void shouldDeactivateCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        // When
        CategoryDto result = categoryService.deactivateCategory(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(sampleCategory.getActive()).isFalse();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(sampleCategory);
        verify(categoryMapper).toDto(sampleCategory);
    }

    @Test
    @DisplayName("Debe actualizar categoría exitosamente")
    void shouldUpdateCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("FPS Updated");
        updateDto.setDescription("Updated description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("FPS Updated", categoryId)).thenReturn(false);
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        // When
        CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByNameIgnoreCaseAndIdNot("FPS Updated", categoryId);
        verify(categoryMapper).updateEntityFromDto(sampleCategory, updateDto);
        verify(categoryRepository).save(sampleCategory);
        verify(categoryMapper).toDto(sampleCategory);
    }

    @Test
    @DisplayName("Debe eliminar categoría exitosamente")
    void shouldDeleteCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);

        // When
        assertThatCode(() -> categoryService.deleteCategory(categoryId))
            .doesNotThrowAnyException();

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(sampleCategory);
    }

    @Test
    @DisplayName("Debe obtener conteo de categorías activas")
    void shouldGetActiveCategoriesCount() {
        // Given
        when(categoryRepository.countByActiveTrue()).thenReturn(5L);

        // When
        long count = categoryService.getActiveCategoriesCount();

        // Then
        assertThat(count).isEqualTo(5L);
        verify(categoryRepository).countByActiveTrue();
    }
} 