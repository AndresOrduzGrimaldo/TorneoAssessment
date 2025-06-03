package com.esport.torneo.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.esport.torneo.application.dto.GameDto;
import com.esport.torneo.domain.game.Game;

/**
 * Mapper para conversión entre entidades Game y DTOs.
 * 
 * Utiliza MapStruct para generar automáticamente las implementaciones
 * de mapeo entre objetos de dominio y DTOs.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Mapper(componentModel = "spring")
public interface GameMapper {

    /**
     * Convierte una entidad Game a GameDto.
     * 
     * @param game la entidad Game
     * @return el GameDto correspondiente
     */
    @Mapping(target = "isTeamGame", source = ".", qualifiedByName = "calculateTeamGame")
    @Mapping(target = "hasImage", source = ".", qualifiedByName = "calculateHasImage")
    @Mapping(target = "isFullyConfigured", source = ".", qualifiedByName = "calculateFullyConfigured")
    GameDto toDto(Game game);

    /**
     * Calcula si el juego es de equipo.
     * 
     * @param game el juego
     * @return true si requiere más de un jugador
     */
    @Named("calculateTeamGame")
    default Boolean calculateTeamGame(Game game) {
        if (game == null || game.getPlayerCount() == null) {
            return false;
        }
        
        return game.getPlayerCount() > 1;
    }

    /**
     * Calcula si el juego tiene imagen.
     * 
     * @param game el juego
     * @return true si tiene URL de imagen configurada
     */
    @Named("calculateHasImage")
    default Boolean calculateHasImage(Game game) {
        if (game == null) {
            return false;
        }
        
        return game.hasImage();
    }

    /**
     * Calcula si el juego está completamente configurado.
     * 
     * @param game el juego
     * @return true si tiene toda la información básica
     */
    @Named("calculateFullyConfigured")
    default Boolean calculateFullyConfigured(Game game) {
        if (game == null) {
            return false;
        }
        
        return game.isFullyConfigured();
    }
} 