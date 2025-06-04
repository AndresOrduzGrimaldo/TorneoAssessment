package com.esport.torneo.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.application.service.CategoryApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de categorías de torneos.
 * 
 * Proporciona endpoints para:
 * - CRUD completo de categorías
 * - Búsquedas y filtros
 * - Gestión de estado activo/inactivo
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Gestión de categorías de torneos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryApplicationService categoryApplicationService;

    /**
     * Constructor del controlador.
     * 
     * @param categoryApplicationService servicio de aplicación de categorías
     */
    public CategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    /**
     * Obtiene todas las categorías activas.
     * 
     * @return lista de categorías activas
     */
    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías activas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        logger.info("Obteniendo todas las categorías activas");
        
        try {
            List<CategoryDto> categories = categoryApplicationService.getAllActiveCategories();
            logger.info("Se encontraron {} categorías activas", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error obteniendo categorías: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene una categoría por ID.
     * 
     * @param id ID de la categoría
     * @return la categoría encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría", description = "Obtiene una categoría específica por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        
        logger.info("Obteniendo categoría con ID: {}", id);
        
        try {
            CategoryDto category = categoryApplicationService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo categoría {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea una nueva categoría.
     * 
     * @param categoryDto datos de la categoría a crear
     * @return la categoría creada
     */
    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Categoría ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(
            @Parameter(description = "Datos de la categoría") @Valid @RequestBody CategoryDto categoryDto) {
        
        logger.info("Creando nueva categoría: {}", categoryDto.getCode());
        
        try {
            CategoryDto createdCategory = categoryApplicationService.createCategory(categoryDto);
            logger.info("Categoría creada exitosamente con ID: {}", createdCategory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Error creando categoría: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error interno creando categoría: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría
     * @param categoryDto datos actualizados
     * @return la categoría actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id,
            @Parameter(description = "Datos actualizados") @Valid @RequestBody CategoryDto categoryDto) {
        
        logger.info("Actualizando categoría con ID: {}", id);
        
        try {
            CategoryDto updatedCategory = categoryApplicationService.updateCategory(id, categoryDto);
            logger.info("Categoría {} actualizada exitosamente", id);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Error actualizando categoría {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error interno actualizando categoría {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina una categoría (soft delete).
     * 
     * @param id ID de la categoría
     * @return confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "Categoría en uso, no se puede eliminar"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        
        logger.info("Eliminando categoría con ID: {}", id);
        
        try {
            categoryApplicationService.deleteCategory(id);
            logger.info("Categoría {} eliminada exitosamente", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada para eliminar: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Categoría {} en uso, no se puede eliminar: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error interno eliminando categoría {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca categorías por descripción.
     * 
     * @param description texto a buscar en la descripción
     * @return lista de categorías que coinciden
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar categorías", description = "Busca categorías por descripción")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<CategoryDto>> searchCategories(
            @Parameter(description = "Texto a buscar en la descripción") 
            @RequestParam String description) {
        
        logger.info("Buscando categorías con descripción: {}", description);
        
        if (description == null || description.trim().isEmpty()) {
            logger.warn("Parámetro de búsqueda vacío");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<CategoryDto> categories = categoryApplicationService.searchCategoriesByDescription(description);
            logger.info("Se encontraron {} categorías con descripción '{}'", categories.size(), description);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error buscando categorías: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de categorías.
     * 
     * @return estadísticas de uso de categorías
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de categorías", description = "Obtiene estadísticas de uso de categorías")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<Object> getCategoryStats() {
        logger.info("Obteniendo estadísticas de categorías");
        
        try {
            Object stats = categoryApplicationService.getCategoryStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de categorías: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 