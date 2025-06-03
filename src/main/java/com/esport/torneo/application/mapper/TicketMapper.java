package com.esport.torneo.application.mapper;

import com.esport.torneo.application.dto.TicketDto;
import com.esport.torneo.domain.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;

/**
 * Mapper para conversión entre entidades Ticket y DTOs.
 * 
 * Utiliza MapStruct para generar automáticamente las implementaciones
 * de mapeo entre objetos de dominio y DTOs.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Mapper(componentModel = "spring")
public interface TicketMapper {

    /**
     * Convierte una entidad Ticket a TicketDto.
     * 
     * @param ticket la entidad Ticket
     * @return el TicketDto correspondiente
     */
    @Mapping(target = "code", source = "ticketCode")
    @Mapping(target = "tournamentId", source = "tournament.id")
    @Mapping(target = "tournamentName", source = "tournament.name")
    @Mapping(target = "qrCodeUrl", source = "qrCode")
    @Mapping(target = "purchaseDate", source = "purchaseDate")
    @Mapping(target = "usedDate", source = "usageDate")
    @Mapping(target = "isExpired", source = ".", qualifiedByName = "calculateIsExpired")
    @Mapping(target = "isUsed", source = ".", qualifiedByName = "calculateIsUsed")
    @Mapping(target = "isValid", source = ".", qualifiedByName = "calculateIsValid")
    @Mapping(target = "hasQrCode", source = ".", qualifiedByName = "calculateHasQrCode")
    @Mapping(target = "daysUntilExpiration", source = ".", qualifiedByName = "calculateDaysUntilExpiration")
    TicketDto toDto(Ticket ticket);

    /**
     * Calcula si el ticket está expirado.
     * 
     * @param ticket el ticket
     * @return true si está expirado
     */
    @Named("calculateIsExpired")
    default Boolean calculateIsExpired(Ticket ticket) {
        if (ticket == null || ticket.getExpirationDate() == null) {
            return false;
        }
        
        return ticket.isExpired();
    }

    /**
     * Calcula si el ticket ha sido usado.
     * 
     * @param ticket el ticket
     * @return true si ha sido usado
     */
    @Named("calculateIsUsed")
    default Boolean calculateIsUsed(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        
        return ticket.getUsageDate() != null;
    }

    /**
     * Calcula si el ticket es válido para uso.
     * 
     * @param ticket el ticket
     * @return true si es válido
     */
    @Named("calculateIsValid")
    default Boolean calculateIsValid(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        
        return ticket.isValidForUse();
    }

    /**
     * Calcula si el ticket tiene código QR.
     * 
     * @param ticket el ticket
     * @return true si tiene código QR
     */
    @Named("calculateHasQrCode")
    default Boolean calculateHasQrCode(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        
        return ticket.getQrCode() != null && !ticket.getQrCode().trim().isEmpty();
    }

    /**
     * Calcula los días hasta la expiración.
     * 
     * @param ticket el ticket
     * @return días hasta expiración
     */
    @Named("calculateDaysUntilExpiration")
    default Long calculateDaysUntilExpiration(Ticket ticket) {
        if (ticket == null || ticket.getExpirationDate() == null || ticket.isExpired()) {
            return 0L;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), ticket.getExpirationDate().toLocalDate());
    }
} 