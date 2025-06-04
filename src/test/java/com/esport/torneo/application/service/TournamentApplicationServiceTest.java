package com.esport.torneo.application.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.esport.torneo.application.dto.TournamentCreateDto;
import com.esport.torneo.application.dto.TournamentDto;
import com.esport.torneo.application.mapper.TournamentMapper;
import com.esport.torneo.domain.category.Category;
import com.esport.torneo.domain.game.Game;
import com.esport.torneo.domain.tournament.Tournament;
import com.esport.torneo.domain.tournament.TournamentStatus;
import com.esport.torneo.domain.tournament.TournamentType;
import com.esport.torneo.infrastructure.repository.CategoryRepository;
import com.esport.torneo.infrastructure.repository.GameRepository;
import com.esport.torneo.infrastructure.repository.TournamentRepository;

/**
 * Tests unitarios para TournamentApplicationService.
 * 
 * Verifica la lógica de aplicación y coordinación entre componentes.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tournament Application Service Tests")
class TournamentApplicationServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private TournamentMapper tournamentMapper;

    @InjectMocks
    private TournamentApplicationService tournamentApplicationService;

    private Category category;
    private Game game;
    private Tournament tournament;
    private TournamentCreateDto createDto;
    private TournamentDto tournamentDto;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        category = new Category("ESPORTS", "E-Sports", "Deportes electrónicos");
        category.setId(1L);

        game = new Game("LOL", "League of Legends", 5, "MOBA game", "MOBA", "PC");
        game.setId(1L);

        futureDate = LocalDateTime.now().plusDays(7);

        tournament = new Tournament(
                "Torneo de Prueba",
                "Descripción del torneo",
                TournamentType.FREE,
                category,
                game,
                1L,
                16,
                futureDate,
                futureDate.plusHours(8),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)
        );
        tournament.setId(1L);

        createDto = new TournamentCreateDto();
        createDto.setName("Nuevo Torneo");
        createDto.setDescription("Descripción del nuevo torneo");
        createDto.setTournamentType(TournamentType.FREE);
        createDto.setCategoryId(1L);
        createDto.setGameId(1L);
        createDto.setOrganizerId(1L);
        createDto.setMaxParticipants(16);
        createDto.setStartDate(futureDate);
        createDto.setEndDate(futureDate.plusHours(8));
        createDto.setRegistrationStart(LocalDateTime.now().plusDays(1));
        createDto.setRegistrationEnd(LocalDateTime.now().plusDays(5));

        tournamentDto = new TournamentDto();
        tournamentDto.setId(1L);
        tournamentDto.setName("Torneo de Prueba");
        tournamentDto.setStatus(TournamentStatus.DRAFT);
    }

    @Test
    @DisplayName("Debe crear torneo exitosamente")
    void shouldCreateTournamentSuccessfully() {
        // Given
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(gameRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(game));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        TournamentDto result = tournamentApplicationService.createTournament(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Torneo de Prueba", result.getName());
        assertEquals(TournamentStatus.DRAFT, result.getStatus());

        verify(categoryRepository).findByIdAndActiveTrue(1L);
        verify(gameRepository).findByIdAndActiveTrue(1L);
        verify(tournamentRepository).save(any(Tournament.class));
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe fallar al crear torneo con categoría inexistente")
    void shouldFailToCreateTournamentWithNonExistentCategory() {
        // Given
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentApplicationService.createTournament(createDto);
        });

        verify(categoryRepository).findByIdAndActiveTrue(1L);
        verify(gameRepository, never()).findByIdAndActiveTrue(anyLong());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Debe fallar al crear torneo con juego inexistente")
    void shouldFailToCreateTournamentWithNonExistentGame() {
        // Given
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(gameRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentApplicationService.createTournament(createDto);
        });

        verify(categoryRepository).findByIdAndActiveTrue(1L);
        verify(gameRepository).findByIdAndActiveTrue(1L);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Debe obtener torneo por ID exitosamente")
    void shouldGetTournamentByIdSuccessfully() {
        // Given
        when(tournamentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(tournament));
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        TournamentDto result = tournamentApplicationService.getTournamentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Torneo de Prueba", result.getName());

        verify(tournamentRepository).findByIdAndActiveTrue(1L);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe fallar al obtener torneo inexistente")
    void shouldFailToGetNonExistentTournament() {
        // Given
        when(tournamentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentApplicationService.getTournamentById(1L);
        });

        verify(tournamentRepository).findByIdAndActiveTrue(1L);
        verify(tournamentMapper, never()).toDto(any(Tournament.class));
    }

    @Test
    @DisplayName("Debe obtener todos los torneos con paginación")
    void shouldGetAllTournamentsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tournament> tournamentPage = new PageImpl<>(Arrays.asList(tournament));
        Page<TournamentDto> expectedPage = new PageImpl<>(Arrays.asList(tournamentDto));

        when(tournamentRepository.findByActiveTrue(pageable)).thenReturn(tournamentPage);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        Page<TournamentDto> result = tournamentApplicationService.getAllTournaments(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(tournamentDto, result.getContent().get(0));

        verify(tournamentRepository).findByActiveTrue(pageable);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe publicar torneo exitosamente")
    void shouldPublishTournamentSuccessfully() {
        // Given
        when(tournamentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(tournament)).thenReturn(tournament);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        TournamentDto result = tournamentApplicationService.publishTournament(1L);

        // Then
        assertNotNull(result);
        assertEquals(TournamentStatus.PUBLISHED, tournament.getStatus());

        verify(tournamentRepository).findByIdAndActiveTrue(1L);
        verify(tournamentRepository).save(tournament);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe registrar participante exitosamente")
    void shouldRegisterParticipantSuccessfully() {
        // Given
        tournament.publish(); // Cambiar estado para permitir registraciones
        when(tournamentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(tournament)).thenReturn(tournament);

        // When
        assertDoesNotThrow(() -> {
            tournamentApplicationService.registerParticipant(1L, 2L, "Team Alpha");
        });

        // Then
        verify(tournamentRepository).findByIdAndActiveTrue(1L);
        verify(tournamentRepository).save(tournament);
    }

    @Test
    @DisplayName("Debe obtener estadísticas de torneos")
    void shouldGetTournamentStats() {
        // Given
        when(tournamentRepository.countByActiveTrue()).thenReturn(10L);
        when(tournamentRepository.countByStatusAndActiveTrue(TournamentStatus.DRAFT)).thenReturn(3L);
        when(tournamentRepository.countByStatusAndActiveTrue(TournamentStatus.PUBLISHED)).thenReturn(4L);
        when(tournamentRepository.countByStatusAndActiveTrue(TournamentStatus.IN_PROGRESS)).thenReturn(2L);
        when(tournamentRepository.countByStatusAndActiveTrue(TournamentStatus.FINISHED)).thenReturn(1L);
        when(tournamentRepository.countByStatusAndActiveTrue(TournamentStatus.CANCELLED)).thenReturn(0L);

        // When
        TournamentApplicationService.TournamentStatsDto stats = 
                tournamentApplicationService.getTournamentStats();

        // Then
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalTournaments());
        assertEquals(3L, stats.getDraftTournaments());
        assertEquals(4L, stats.getPublishedTournaments());
        assertEquals(2L, stats.getInProgressTournaments());
        assertEquals(1L, stats.getFinishedTournaments());
        assertEquals(0L, stats.getCancelledTournaments());

        verify(tournamentRepository).countByActiveTrue();
        verify(tournamentRepository, times(5)).countByStatusAndActiveTrue(any(TournamentStatus.class));
    }

    @Test
    @DisplayName("Debe buscar torneos por nombre con paginación")
    void shouldSearchTournamentsByNameWithPagination() {
        // Given
        String searchName = "League";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tournament> tournamentPage = new PageImpl<>(Arrays.asList(tournament));

        when(tournamentRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchName, pageable))
                .thenReturn(tournamentPage);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        Page<TournamentDto> result = tournamentApplicationService.searchTournamentsByName(searchName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(tournamentRepository).findByNameContainingIgnoreCaseAndActiveTrue(searchName, pageable);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe obtener torneos por categoría")
    void shouldGetTournamentsByCategory() {
        // Given
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tournament> tournamentPage = new PageImpl<>(Arrays.asList(tournament));

        when(tournamentRepository.findByCategoryIdAndActiveTrue(categoryId, pageable))
                .thenReturn(tournamentPage);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        Page<TournamentDto> result = tournamentApplicationService.getTournamentsByCategory(categoryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(tournamentRepository).findByCategoryIdAndActiveTrue(categoryId, pageable);
        verify(tournamentMapper).toDto(tournament);
    }

    @Test
    @DisplayName("Debe obtener torneos próximos")
    void shouldGetUpcomingTournaments() {
        // Given
        LocalDateTime endTime = LocalDateTime.now().plusDays(30);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tournament> tournamentPage = new PageImpl<>(Arrays.asList(tournament));

        when(tournamentRepository.findByStartDateBetweenAndStatusAndActiveTrue(
                any(LocalDateTime.class), eq(endTime), eq(TournamentStatus.PUBLISHED), eq(pageable)))
                .thenReturn(tournamentPage);
        when(tournamentMapper.toDto(tournament)).thenReturn(tournamentDto);

        // When
        Page<TournamentDto> result = tournamentApplicationService.getUpcomingTournaments(endTime, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(tournamentRepository).findByStartDateBetweenAndStatusAndActiveTrue(
                any(LocalDateTime.class), eq(endTime), eq(TournamentStatus.PUBLISHED), eq(pageable));
        verify(tournamentMapper).toDto(tournament);
    }
} 