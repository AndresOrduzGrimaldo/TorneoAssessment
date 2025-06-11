package com.esport.torneo.application.mapper;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.domain.category.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Category y DTOs.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Component
public class CategoryMapper {

    /**
     * Convierte una entidad Category a CategoryDto.
     *
     * @param category la entidad a convertir
     * @return el DTO correspondiente
     */
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    /**
     * Convierte un CategoryDto a entidad Category.
     *
     * @param dto el DTO a convertir
     * @return la entidad correspondiente
     */
    public Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category(dto.getName(), dto.getDescription());
        category.setId(dto.getId());
        category.setActive(dto.getActive());
        
        return category;
    }

    /**
     * Actualiza una entidad Category existente con datos del DTO.
     *
     * @param category la entidad a actualizar
     * @param dto el DTO con los nuevos datos
     */
    public void updateEntityFromDto(Category category, CategoryDto dto) {
        if (category == null || dto == null) {
            return;
        }

        if (dto.getName() != null) {
            category.updateName(dto.getName());
        }
        if (dto.getDescription() != null) {
            category.updateDescription(dto.getDescription());
        }
        if (dto.getActive() != null) {
            if (dto.getActive()) {
                category.activate();
            } else {
                category.deactivate();
            }
        }
    }
} 