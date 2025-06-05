package com.esport.torneo.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import com.esport.torneo.infrastructure.config.RedisConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for Category management.
 * Provides endpoints for CRUD operations on tournament categories.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Tournament category management APIs")
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
     * Get all categories with pagination.
     */
    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Retrieve a paginated list of all active tournament categories"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.CATEGORIES_CACHE, key = "'all_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @PageableDefault(size = 20) Pageable pageable) {
        List<CategoryDto> categories = categoryApplicationService.getAllActiveCategories();
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), categories.size());
        Page<CategoryDto> categoriesPage = new PageImpl<>(
            categories.subList(start, end), 
            pageable, 
            categories.size()
        );
        
        return ResponseEntity.ok(categoriesPage);
    }

    /**
     * Get all categories as a simple list (for dropdowns).
     */
    @GetMapping("/list")
    @Operation(
        summary = "Get categories list",
        description = "Retrieve a simple list of all active categories for dropdown/select components"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories list retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.CATEGORIES_CACHE, key = "'all_list'")
    public ResponseEntity<List<CategoryDto>> getCategoriesList() {
        List<CategoryDto> categories = categoryApplicationService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieve a specific category by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Cacheable(value = RedisConfig.CATEGORIES_CACHE, key = "'by_id_' + #id")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        try {
            CategoryDto category = categoryApplicationService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search categories by description.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search categories",
        description = "Search categories by description (case insensitive, partial match)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<CategoryDto>> searchCategories(
            @Parameter(description = "Search term for category description")
            @RequestParam String description,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<CategoryDto> categories = categoryApplicationService.searchCategoriesByDescription(description);
        
        // Create a page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), categories.size());
        Page<CategoryDto> categoriesPage = new PageImpl<>(
            categories.subList(start, end), 
            pageable, 
            categories.size()
        );
        
        return ResponseEntity.ok(categoriesPage);
    }

    /**
     * Create a new category.
     */
    @PostMapping
    @Operation(
        summary = "Create category",
        description = "Create a new tournament category. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "409", description = "Category code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.CATEGORIES_CACHE, allEntries = true)
    public ResponseEntity<CategoryDto> createCategory(
            @Parameter(description = "Category data", required = true)
            @Valid @RequestBody CategoryDto request) {
        try {
            CategoryDto category = categoryApplicationService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Update an existing category.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update category",
        description = "Update an existing tournament category. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.CATEGORIES_CACHE, allEntries = true)
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated category data", required = true)
            @Valid @RequestBody CategoryDto request) {
        try {
            CategoryDto category = categoryApplicationService.updateCategory(id, request);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a category (soft delete).
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete category",
        description = "Soft delete a tournament category. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category is being used by tournaments"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = RedisConfig.CATEGORIES_CACHE, allEntries = true)
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        try {
            categoryApplicationService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get category statistics.
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get category statistics",
        description = "Get statistics about categories usage. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getCategoryStats() {
        Object stats = categoryApplicationService.getCategoryStats();
        return ResponseEntity.ok(stats);
    }
} 