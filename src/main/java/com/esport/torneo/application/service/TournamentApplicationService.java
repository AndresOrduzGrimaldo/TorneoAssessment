package com.esport.torneo.application.service;

import com.esport.torneo.application.dto.TournamentDto;
import com.esport.torneo.application.dto.TournamentCreateDto;
import com.esport.torneo.application.dto.TournamentUpdateDto;
import com.esport.torneo.application.mapper.TournamentMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.domain.game.Game;
import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.domain.tournament.TournamentType;
import com.esport.torneo.infrastructure.repository.CategoryRepository;
import com.esport.torneo.infrastructure.repository.GameRepository;
import com.esport.torneo.infrastructure.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para gestionar torneos.
 * 
 * Coordina las operaciones de negocio relacionadas con torneos,
 * actuando como capa de aplicación en la arquitectura DDD.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class TournamentApplicationService {

    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final GameRepository gameRepository;
    private final TournamentMapper tournamentMapper;

    @Autowired
    public TournamentApplicationService(TournamentRepository tournamentRepository,
                                       CategoryRepository categoryRepository,
                                       GameRepository gameRepository,
                                       TournamentMapper tournamentMapper) {
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.gameRepository = gameRepository;
        this.tournamentMapper = tournamentMapper;
    }

    /**
     * Crea un nuevo torneo.
     * 
     * @param createDto datos para crear el torneo
     * @return el torneo creado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public TournamentDto createTournament(TournamentCreateDto createDto) {
        // Validar categoría
        Category category = categoryRepository.findByIdAndActiveTrue(createDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));

        // Validar juego
        Game game = gameRepository.findByIdAndActiveTrue(createDto.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado o inactivo"));

        // Crear torneo
        Tournament tournament = new Tournament(
                createDto.getName(),
                createDto.getDescription(),
                createDto.getTournamentType(),
                category,
                game,
                createDto.getOrganizerId(),
                createDto.getMaxParticipants(),
                createDto.getStartDate(),
                createDto.getEndDate(),
                createDto.getRegistrationStart(),
                createDto.getRegistrationEnd()
        );

        // Configurar campos opcionales
        if (createDto.getEntryFee() != null) {
            tournament.setEntryFee(createDto.getEntryFee());
        }
        if (createDto.getPrizePool() != null) {
            tournament.setPrizePool(createDto.getPrizePool());
        }
        if (createDto.getCommissionRate() != null) {
            tournament.setCommissionRate(createDto.getCommissionRate());
        }
        if (createDto.getStreamUrl() != null) {
            tournament.configureStreaming(createDto.getStreamUrl(), createDto.getStreamPlatform());
        }
        if (createDto.getRules() != null) {
            tournament.setRules(createDto.getRules());
        }
        if (createDto.getBannerImageUrl() != null) {
            tournament.setBannerImageUrl(createDto.getBannerImageUrl());
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Actualiza un torneo existente.
     * 
     * @param id ID del torneo
     * @param updateDto datos de actualización
     * @return el torneo actualizado
     * @throws IllegalArgumentException si el torneo no existe o no se puede actualizar
     */
    public TournamentDto updateTournament(Long id, TournamentUpdateDto updateDto) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        // Solo permitir actualización en estado DRAFT
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden actualizar torneos en estado borrador");
        }

        // Actualizar información básica
        if (updateDto.getName() != null) {
            tournament.updateBasicInfo(
                    updateDto.getName(),
                    updateDto.getDescription(),
                    updateDto.getMaxParticipants(),
                    updateDto.getEntryFee(),
                    updateDto.getPrizePool()
            );
        }

        // Actualizar fechas
        if (updateDto.getStartDate() != null) {
            tournament.updateDates(
                    updateDto.getStartDate(),
                    updateDto.getEndDate(),
                    updateDto.getRegistrationStart(),
                    updateDto.getRegistrationEnd()
            );
        }

        // Actualizar streaming
        if (updateDto.getStreamUrl() != null) {
            tournament.configureStreaming(updateDto.getStreamUrl(), updateDto.getStreamPlatform());
        }

        // Actualizar otros campos
        if (updateDto.getRules() != null) {
            tournament.setRules(updateDto.getRules());
        }
        if (updateDto.getBannerImageUrl() != null) {
            tournament.setBannerImageUrl(updateDto.getBannerImageUrl());
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Busca un torneo por ID.
     * 
     * @param id ID del torneo
     * @return el torneo si existe
     */
    @Transactional(readOnly = true)
    public Optional<TournamentDto> findTournamentById(Long id) {
        return tournamentRepository.findByIdAndActiveTrue(id)
                .map(tournamentMapper::toDto);
    }

    /**
     * Busca todos los torneos activos con paginación.
     * 
     * @param pageable configuración de paginación
     * @return página de torneos
     */
    @Transactional(readOnly = true)
    public Page<TournamentDto> findAllTournaments(Pageable pageable) {
        return tournamentRepository.findByActiveTrue(pageable)
                .map(tournamentMapper::toDto);
    }

    /**
     * Busca torneos por estado.
     * 
     * @param status estado del torneo
     * @param pageable configuración de paginación
     * @return página de torneos
     */
    @Transactional(readOnly = true)
    public Page<TournamentDto> findTournamentsByStatus(TournamentStatus status, Pageable pageable) {
        return tournamentRepository.findByStatusAndActiveTrue(status, pageable)
                .map(tournamentMapper::toDto);
    }

    /**
     * Busca torneos por organizador.
     * 
     * @param organizerId ID del organizador
     * @return lista de torneos del organizador
     */
    @Transactional(readOnly = true)
    public List<TournamentDto> findTournamentsByOrganizer(Long organizerId) {
        return tournamentRepository.findByOrganizerIdAndActiveTrueOrderByStartDateDesc(organizerId)
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    /**
     * Busca torneos disponibles para registro.
     * 
     * @return lista de torneos abiertos para registro
     */
    @Transactional(readOnly = true)
    public List<TournamentDto> findTournamentsOpenForRegistration() {
        LocalDateTime now = LocalDateTime.now();
        return tournamentRepository.findTournamentsOpenForRegistration(now)
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    /**
     * Busca torneos que inician pronto.
     * 
     * @param hours número de horas hacia adelante
     * @return lista de torneos próximos a iniciar
     */
    @Transactional(readOnly = true)
    public List<TournamentDto> findTournamentsStartingSoon(int hours) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusHours(hours);
        return tournamentRepository.findTournamentsStartingSoon(from, to)
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    /**
     * Publica un torneo.
     * 
     * @param id ID del torneo
     * @return el torneo publicado
     * @throws IllegalArgumentException si el torneo no existe
     * @throws IllegalStateException si el torneo no puede ser publicado
     */
    public TournamentDto publishTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.publish();
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Inicia un torneo.
     * 
     * @param id ID del torneo
     * @return el torneo iniciado
     * @throws IllegalArgumentException si el torneo no existe
     * @throws IllegalStateException si el torneo no puede ser iniciado
     */
    public TournamentDto startTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.start();
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Finaliza un torneo.
     * 
     * @param id ID del torneo
     * @return el torneo finalizado
     * @throws IllegalArgumentException si el torneo no existe
     * @throws IllegalStateException si el torneo no puede ser finalizado
     */
    public TournamentDto finishTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.finish();
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Cancela un torneo.
     * 
     * @param id ID del torneo
     * @return el torneo cancelado
     * @throws IllegalArgumentException si el torneo no existe
     * @throws IllegalStateException si el torneo no puede ser cancelado
     */
    public TournamentDto cancelTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.cancel();
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(savedTournament);
    }

    /**
     * Elimina lógicamente un torneo.
     * 
     * @param id ID del torneo
     * @throws IllegalArgumentException si el torneo no existe
     * @throws IllegalStateException si el torneo no puede ser eliminado
     */
    public void deleteTournament(Long id) {
        Tournament tournament = tournamentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        // Solo permitir eliminación en estados específicos
        if (tournament.getStatus() == TournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("No se puede eliminar un torneo en progreso");
        }

        tournament.markAsDeleted();
        tournamentRepository.save(tournament);
    }

    /**
     * Busca torneos por nombre.
     * 
     * @param name nombre a buscar
     * @return lista de torneos que coinciden
     */
    @Transactional(readOnly = true)
    public List<TournamentDto> searchTournamentsByName(String name) {
        return tournamentRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    /**
     * Busca torneos gratuitos.
     * 
     * @return lista de torneos gratuitos
     */
    @Transactional(readOnly = true)
    public List<TournamentDto> findFreeTournaments() {
        return tournamentRepository.findFreeTournaments()
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    /**
     * Cuenta torneos por estado.
     * 
     * @param status estado del torneo
     * @return número de torneos en el estado
     */
    @Transactional(readOnly = true)
    public long countTournamentsByStatus(TournamentStatus status) {
        return tournamentRepository.countByStatusAndActiveTrue(status);
    }

    /**
     * Busca estadísticas de participación.
     * 
     * @return lista de estadísticas de participación
     */
    @Transactional(readOnly = true)
    public List<Object[]> findParticipationStats() {
        return tournamentRepository.findParticipationStats();
    }
} 