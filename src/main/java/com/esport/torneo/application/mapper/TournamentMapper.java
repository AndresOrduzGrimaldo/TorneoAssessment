package com.esport.torneo.application.mapper;

import com.esport.torneo.application.dto.TournamentDto;
import com.esport.torneo.application.dto.TournamentCreateDto;
import com.esport.torneo.domain.tournament.Tournament;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mapper para conversión entre entidades Tournament y DTOs.
 * 
 * Utiliza MapStruct para generar automáticamente las implementaciones
 * de mapeo entre objetos de dominio y DTOs.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Mapper(componentModel = "spring")
public interface TournamentMapper {

    /**
     * Convierte una entidad Tournament a TournamentDto.
     * 
     * @param tournament la entidad Tournament
     * @return el TournamentDto correspondiente
     */
    @Mapping(target = "isRegistrationOpen", source = ".", qualifiedByName = "calculateRegistrationOpen")
    @Mapping(target = "hasAvailableSlots", source = ".", qualifiedByName = "calculateAvailableSlots")
    @Mapping(target = "totalCommission", source = ".", qualifiedByName = "calculateTotalCommission")
    TournamentDto toDto(Tournament tournament);

    /**
     * Calcula si el período de registro está abierto.
     * 
     * @param tournament el torneo
     * @return true si el registro está abierto
     */
    @Named("calculateRegistrationOpen")
    default Boolean calculateRegistrationOpen(Tournament tournament) {
        if (tournament == null || tournament.getRegistrationStart() == null || 
            tournament.getRegistrationEnd() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return tournament.getRegistrationStart().isBefore(now) && 
               tournament.getRegistrationEnd().isAfter(now) &&
               tournament.getStatus().toString().equals("PUBLISHED");
    }

    /**
     * Calcula si hay cupos disponibles.
     * 
     * @param tournament el torneo
     * @return true si hay cupos disponibles
     */
    @Named("calculateAvailableSlots")
    default Boolean calculateAvailableSlots(Tournament tournament) {
        if (tournament == null || tournament.getMaxParticipants() == null || 
            tournament.getCurrentParticipants() == null) {
            return false;
        }
        
        return tournament.getCurrentParticipants() < tournament.getMaxParticipants();
    }

    /**
     * Calcula la comisión total del torneo.
     * 
     * @param tournament el torneo
     * @return la comisión total calculada
     */
    @Named("calculateTotalCommission")
    default BigDecimal calculateTotalCommission(Tournament tournament) {
        if (tournament == null || tournament.getEntryFee() == null || 
            tournament.getCommissionRate() == null || tournament.getCurrentParticipants() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalRevenue = tournament.getEntryFee()
                .multiply(BigDecimal.valueOf(tournament.getCurrentParticipants()));
        return totalRevenue.multiply(tournament.getCommissionRate());
    }
} 