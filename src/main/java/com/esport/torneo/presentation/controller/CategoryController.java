package com.esport.torneo.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.esport.torneo.application.service.CategoryApplicationService.CategoryStatsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de categorías.
 * 
 * Proporciona endpoints para operaciones CRUD y consultas
 * relacionadas con categorías de torneos.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "API para gestión de categorías de torneos")
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
     * Crea una nueva categoría.
     * 
     * @param categoryDto datos de la categoría a crear
     * @return la categoría creada
     */
    @PostMapping
    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe una categoría con el mismo código")
    })
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto) {
        
        logger.info("Creando nueva categoría: {}", categoryDto.getCode());
        
        try {
            CategoryDto createdCategory = categoryApplicationService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Error creando categoría: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene una categoría por ID.
     * 
     * @param id ID de la categoría
     * @return la categoría encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        
        logger.debug("Obteniendo categoría por ID: {}", id);
        
        try {
            CategoryDto category = categoryApplicationService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene una categoría por código.
     * 
     * @param code código de la categoría
     * @return la categoría encontrada
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener categoría por código", description = "Obtiene una categoría específica por su código único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoryDto> getCategoryByCode(
            @Parameter(description = "Código de la categoría") @PathVariable String code) {
        
        logger.debug("Obteniendo categoría por código: {}", code);
        
        try {
            CategoryDto category = categoryApplicationService.getCategoryByCode(code);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada: {}", code);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todas las categorías activas.
     * 
     * @return lista de categorías activas
     */
    @GetMapping
    @Operation(summary = "Listar categorías activas", description = "Obtiene todas las categorías activas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
    public ResponseEntity<List<CategoryDto>> getAllActiveCategories() {
        logger.debug("Obteniendo todas las categorías activas");
        
        List<CategoryDto> categories = categoryApplicationService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Busca categorías por descripción.
     * 
     * @param description descripción a buscar
     * @return lista de categorías que coinciden
     */
    @GetMapping("/search/description")
    @Operation(summary = "Buscar categorías por descripción", description = "Busca categorías que contengan el texto especificado en la descripción")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    public ResponseEntity<List<CategoryDto>> searchCategoriesByDescription(
            @Parameter(description = "Texto a buscar en la descripción") @RequestParam String description) {
        
        logger.debug("Buscando categorías por descripción: {}", description);
        
        List<CategoryDto> categories = categoryApplicationService.searchCategoriesByDescription(description);
        return ResponseEntity.ok(categories);
    }

    /**
     * Busca categorías por alias.
     * 
     * @param alias alias a buscar
     * @return lista de categorías que coinciden
     */
    @GetMapping("/search/alias")
    @Operation(summary = "Buscar categorías por alias", description = "Busca categorías que contengan el texto especificado en el alias")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    public ResponseEntity<List<CategoryDto>> searchCategoriesByAlias(
            @Parameter(description = "Texto a buscar en el alias") @RequestParam String alias) {
        
        logger.debug("Buscando categorías por alias: {}", alias);
        
        List<CategoryDto> categories = categoryApplicationService.searchCategoriesByAlias(alias);
        return ResponseEntity.ok(categories);
    }

    /**
     * Obtiene categorías que tienen alias definido.
     * 
     * @return lista de categorías con alias
     */
    @GetMapping("/with-alias")
    @Operation(summary = "Obtener categorías con alias", description = "Obtiene todas las categorías que tienen alias definido")
    @ApiResponse(responseCode = "200", description = "Lista de categorías con alias obtenida exitosamente")
    public ResponseEntity<List<CategoryDto>> getCategoriesWithAlias() {
        logger.debug("Obteniendo categorías con alias");
        
        List<CategoryDto> categories = categoryApplicationService.getCategoriesWithAlias();
        return ResponseEntity.ok(categories);
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría a actualizar
     * @param categoryDto datos actualizados
     * @return la categoría actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto con código existente")
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto) {
        
        logger.info("Actualizando categoría: {}", id);
        
        try {
            CategoryDto updatedCategory = categoryApplicationService.updateCategory(id, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Error actualizando categoría: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una categoría (soft delete).
     * 
     * @param id ID de la categoría a eliminar
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar la categoría")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        
        logger.info("Eliminando categoría: {}", id);
        
        try {
            categoryApplicationService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar la categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Reactiva una categoría eliminada.
     * 
     * @param id ID de la categoría a reactivar
     * @return la categoría reactivada
     */
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivar categoría", description = "Reactiva una categoría previamente eliminada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría reactivada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "La categoría ya está activa")
    })
    public ResponseEntity<CategoryDto> reactivateCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        
        logger.info("Reactivando categoría: {}", id);
        
        try {
            CategoryDto reactivatedCategory = categoryApplicationService.reactivateCategory(id);
            return ResponseEntity.ok(reactivatedCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Categoría no encontrada: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("La categoría ya está activa: {}", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Obtiene estadísticas de categorías.
     * 
     * @return estadísticas generales
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de categorías", description = "Obtiene estadísticas generales sobre las categorías del sistema")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<CategoryStatsDto> getCategoryStats() {
        logger.debug("Obteniendo estadísticas de categorías");
        
        CategoryStatsDto stats = categoryApplicationService.getCategoryStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Verifica si existe una categoría con el código dado.
     * 
     * @param code código a verificar
     * @return respuesta con el resultado de la verificación
     */
    @GetMapping("/exists/{code}")
    @Operation(summary = "Verificar existencia de categoría", description = "Verifica si existe una categoría con el código especificado")
    @ApiResponse(responseCode = "200", description = "Verificación completada")
    public ResponseEntity<Boolean> existsByCode(
            @Parameter(description = "Código de la categoría") @PathVariable String code) {
        
        logger.debug("Verificando existencia de categoría: {}", code);
        
        boolean exists = categoryApplicationService.existsByCode(code);
        return ResponseEntity.ok(exists);
    }
} 