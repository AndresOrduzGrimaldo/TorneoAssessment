package com.esport.torneo.application.mapper;

import com.esport.torneo.application.dto.GameDto;
import com.esport.torneo.domain.game.Game;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Game y DTOs.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Component
public class GameMapper {

    /**
     * Convierte una entidad Game a GameDto.
     *
     * @param game la entidad a convertir
     * @return el DTO correspondiente
     */
    public GameDto toDto(Game game) {
        if (game == null) {
            return null;
        }

        return new GameDto(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getMinPlayers(),
                game.getMaxPlayers(),
                game.getGenre(),
                game.getPlatform(),
                game.getDeveloper(),
                game.getImageUrl(),
                game.getActive(),
                game.getCategory() != null ? game.getCategory().getId() : null,
                game.getCategory() != null ? game.getCategory().getName() : null,
                game.getCreatedAt(),
                game.getUpdatedAt()
        );
    }

    /**
     * Convierte un GameDto a entidad Game (sin categoría).
     *
     * @param dto el DTO a convertir
     * @return la entidad correspondiente
     */
    public Game toEntity(GameDto dto) {
        if (dto == null) {
            return null;
        }

        Game game = new Game(
                dto.getName(),
                dto.getDescription(),
                dto.getMinPlayers(),
                dto.getMaxPlayers(),
                null // La categoría se asigna por separado
        );
        
        game.setId(dto.getId());
        game.setGenre(dto.getGenre());
        game.setPlatform(dto.getPlatform());
        game.setDeveloper(dto.getDeveloper());
        game.setImageUrl(dto.getImageUrl());
        game.setActive(dto.getActive());
        
        return game;
    }

    /**
     * Actualiza una entidad Game existente con datos del DTO.
     *
     * @param game la entidad a actualizar
     * @param dto el DTO con los nuevos datos
     */
    public void updateEntityFromDto(Game game, GameDto dto) {
        if (game == null || dto == null) {
            return;
        }

        if (dto.getName() != null) {
            game.updateName(dto.getName());
        }
        if (dto.getDescription() != null) {
            game.updateDescription(dto.getDescription());
        }
        if (dto.getMinPlayers() != null && dto.getMaxPlayers() != null) {
            game.updatePlayerLimits(dto.getMinPlayers(), dto.getMaxPlayers());
        }
        if (dto.getGenre() != null) {
            game.setGenre(dto.getGenre());
        }
        if (dto.getPlatform() != null) {
            game.setPlatform(dto.getPlatform());
        }
        if (dto.getDeveloper() != null) {
            game.setDeveloper(dto.getDeveloper());
        }
        if (dto.getImageUrl() != null) {
            game.setImageUrl(dto.getImageUrl());
        }
        if (dto.getActive() != null) {
            if (dto.getActive()) {
                game.activate();
            } else {
                game.deactivate();
            }
        }
    }
} 