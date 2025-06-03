package com.esport.torneo.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.esport.torneo.application.dto.CategoryDto;
import com.esport.torneo.domain.category.Category;

/**
 * Mapper para conversión entre entidades Category y DTOs.
 * 
 * Utiliza MapStruct para generar automáticamente las implementaciones
 * de mapeo entre objetos de dominio y DTOs.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Convierte una entidad Category a CategoryDto.
     * 
     * @param category la entidad Category
     * @return el CategoryDto correspondiente
     */
    @Mapping(target = "displayName", source = ".", qualifiedByName = "calculateDisplayName")
    CategoryDto toDto(Category category);

    /**
     * Calcula el nombre de visualización de la categoría.
     * 
     * @param category la categoría
     * @return el alias si está disponible, sino la descripción
     */
    @Named("calculateDisplayName")
    default String calculateDisplayName(Category category) {
        if (category == null) {
            return null;
        }
        
        return category.getDisplayName();
    }
} 