package com.esport.torneo.domain.tournament;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.esport.torneo.domain.category.Category;
import com.esport.torneo.domain.game.Game;

/**
 * Tests unitarios para la entidad Tournament.
 * 
 * Verifica las reglas de negocio y validaciones del dominio.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@DisplayName("Tournament Domain Tests")
class TournamentTest {

    private Category category;
    private Game game;
    private LocalDateTime now;
    private LocalDateTime futureDate;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        category = new Category("ESPORTS", "E-Sports", "Deportes electrónicos");
        game = new Game("LOL", "League of Legends", 5, "MOBA game", "MOBA", "PC");
        
        now = LocalDateTime.now();
        futureDate = now.plusDays(7);
        registrationStart = now.plusDays(1);
        registrationEnd = now.plusDays(5);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Debe crear torneo gratuito válido")
        void shouldCreateValidFreeTournament() {
            // Given & When
            Tournament tournament = new Tournament(
                    "Torneo de Prueba",
                    "Descripción del torneo",
                    TournamentType.FREE,
                    category,
                    game,
                    1L,
                    16,
                    futureDate,
                    futureDate.plusHours(8),
                    registrationStart,
                    registrationEnd
            );

            // Then
            assertNotNull(tournament);
            assertEquals("Torneo de Prueba", tournament.getName());
            assertEquals(TournamentType.FREE, tournament.getTournamentType());
            assertEquals(TournamentStatus.DRAFT, tournament.getStatus());
            assertEquals(BigDecimal.ZERO, tournament.getEntryFee());
            assertEquals(0, tournament.getCurrentParticipants());
        }

        @Test
        @DisplayName("Debe fallar si fecha de fin es anterior a fecha de inicio")
        void shouldFailWhenEndDateBeforeStartDate() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Tournament(
                        "Torneo Inválido",
                        "Descripción",
                        TournamentType.FREE,
                        category,
                        game,
                        1L,
                        16,
                        futureDate,
                        futureDate.minusHours(1), // Fecha de fin anterior
                        registrationStart,
                        registrationEnd
                );
            });
        }

        @Test
        @DisplayName("Debe fallar si registraciones terminan después del inicio del torneo")
        void shouldFailWhenRegistrationEndAfterTournamentStart() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Tournament(
                        "Torneo Inválido",
                        "Descripción",
                        TournamentType.FREE,
                        category,
                        game,
                        1L,
                        16,
                        futureDate,
                        futureDate.plusHours(8),
                        registrationStart,
                        futureDate.plusHours(1) // Registraciones terminan después del inicio
                );
            });
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        private Tournament tournament;

        @BeforeEach
        void setUp() {
            tournament = new Tournament(
                    "Torneo de Estado",
                    "Descripción",
                    TournamentType.FREE,
                    category,
                    game,
                    1L,
                    16,
                    futureDate,
                    futureDate.plusHours(8),
                    registrationStart,
                    registrationEnd
            );
        }

        @Test
        @DisplayName("Debe publicar torneo desde estado DRAFT")
        void shouldPublishFromDraft() {
            // Given
            assertEquals(TournamentStatus.DRAFT, tournament.getStatus());

            // When
            tournament.publish();

            // Then
            assertEquals(TournamentStatus.PUBLISHED, tournament.getStatus());
        }

        @Test
        @DisplayName("Debe iniciar torneo desde estado PUBLISHED con participantes suficientes")
        void shouldStartFromPublishedWithEnoughParticipants() {
            // Given
            tournament.publish();
            tournament.setCurrentParticipants(5); // Más de 2 participantes

            // When
            tournament.start();

            // Then
            assertEquals(TournamentStatus.IN_PROGRESS, tournament.getStatus());
        }

        @Test
        @DisplayName("Debe fallar al iniciar torneo sin participantes suficientes")
        void shouldFailToStartWithoutEnoughParticipants() {
            // Given
            tournament.publish();
            tournament.setCurrentParticipants(1); // Solo 1 participante

            // When & Then
            assertThrows(IllegalStateException.class, () -> tournament.start());
        }

        @Test
        @DisplayName("Debe finalizar torneo desde estado IN_PROGRESS")
        void shouldFinishFromInProgress() {
            // Given
            tournament.publish();
            tournament.setCurrentParticipants(5);
            tournament.start();

            // When
            tournament.finish();

            // Then
            assertEquals(TournamentStatus.FINISHED, tournament.getStatus());
        }

        @Test
        @DisplayName("Debe cancelar torneo desde estados válidos")
        void shouldCancelFromValidStates() {
            // Given - Estado DRAFT
            assertEquals(TournamentStatus.DRAFT, tournament.getStatus());

            // When
            tournament.cancel();

            // Then
            assertEquals(TournamentStatus.CANCELLED, tournament.getStatus());
        }
    }

    @Nested
    @DisplayName("Participant Management Tests")
    class ParticipantManagementTests {

        private Tournament tournament;

        @BeforeEach
        void setUp() {
            tournament = new Tournament(
                    "Torneo Participantes",
                    "Descripción",
                    TournamentType.FREE,
                    category,
                    game,
                    1L,
                    4, // Máximo 4 participantes
                    futureDate,
                    futureDate.plusHours(8),
                    now.minusHours(1), // Registraciones ya iniciadas
                    futureDate.minusHours(1) // Registraciones abiertas
            );
            tournament.publish(); // Necesario para permitir registraciones
        }

        @Test
        @DisplayName("Debe agregar participante cuando hay cupos disponibles")
        void shouldAddParticipantWhenSlotsAvailable() {
            // Given
            assertTrue(tournament.canRegisterParticipants());

            // When
            tournament.addParticipant(1L, "Team Alpha");

            // Then
            assertEquals(1, tournament.getCurrentParticipants());
            assertEquals(1, tournament.getParticipants().size());
        }

        @Test
        @DisplayName("Debe fallar al agregar participante cuando no hay cupos")
        void shouldFailToAddParticipantWhenNoSlots() {
            // Given
            tournament.setCurrentParticipants(4); // Torneo lleno

            // When & Then
            assertThrows(IllegalStateException.class, () -> {
                tournament.addParticipant(5L, "Team Overflow");
            });
        }

        @Test
        @DisplayName("Debe verificar correctamente si hay cupos disponibles")
        void shouldCheckAvailableSlotsCorrectly() {
            // Given
            tournament.setCurrentParticipants(3);

            // When & Then
            assertTrue(tournament.hasAvailableSlots());

            // Given
            tournament.setCurrentParticipants(4);

            // When & Then
            assertFalse(tournament.hasAvailableSlots());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        private Tournament paidTournament;

        @BeforeEach
        void setUp() {
            paidTournament = new Tournament(
                    "Torneo de Pago",
                    "Descripción",
                    TournamentType.PAID,
                    category,
                    game,
                    1L,
                    16,
                    futureDate,
                    futureDate.plusHours(8),
                    registrationStart,
                    registrationEnd
            );
            paidTournament.setEntryFee(new BigDecimal("50.00"));
            paidTournament.setCommissionRate(new BigDecimal("0.05")); // 5%
        }

        @Test
        @DisplayName("Debe calcular comisión correctamente para torneo de pago")
        void shouldCalculateCommissionForPaidTournament() {
            // Given
            paidTournament.setCurrentParticipants(10);

            // When
            BigDecimal commission = paidTournament.calculateTotalCommission();

            // Then
            // 10 participantes * $50 * 5% = $25
            assertEquals(new BigDecimal("25.00"), commission);
        }

        @Test
        @DisplayName("Debe retornar cero comisión para torneo gratuito")
        void shouldReturnZeroCommissionForFreeTournament() {
            // Given
            Tournament freeTournament = new Tournament(
                    "Torneo Gratuito",
                    "Descripción",
                    TournamentType.FREE,
                    category,
                    game,
                    1L,
                    16,
                    futureDate,
                    futureDate.plusHours(8),
                    registrationStart,
                    registrationEnd
            );
            freeTournament.setCurrentParticipants(10);

            // When
            BigDecimal commission = freeTournament.calculateTotalCommission();

            // Then
            assertEquals(BigDecimal.ZERO, commission);
        }

        @Test
        @DisplayName("Debe actualizar información básica solo en estado DRAFT")
        void shouldUpdateBasicInfoOnlyInDraft() {
            // Given
            assertEquals(TournamentStatus.DRAFT, paidTournament.getStatus());

            // When
            paidTournament.updateBasicInfo(
                    "Nuevo Nombre",
                    "Nueva Descripción",
                    20,
                    new BigDecimal("75.00"),
                    new BigDecimal("1000.00")
            );

            // Then
            assertEquals("Nuevo Nombre", paidTournament.getName());
            assertEquals("Nueva Descripción", paidTournament.getDescription());
            assertEquals(20, paidTournament.getMaxParticipants());
            assertEquals(new BigDecimal("75.00"), paidTournament.getEntryFee());
            assertEquals(new BigDecimal("1000.00"), paidTournament.getPrizePool());
        }

        @Test
        @DisplayName("Debe fallar al actualizar información básica fuera de estado DRAFT")
        void shouldFailToUpdateBasicInfoOutsideDraft() {
            // Given
            paidTournament.publish();

            // When & Then
            assertThrows(IllegalStateException.class, () -> {
                paidTournament.updateBasicInfo(
                        "Nuevo Nombre",
                        "Nueva Descripción",
                        20,
                        new BigDecimal("75.00"),
                        new BigDecimal("1000.00")
                );
            });
        }

        @Test
        @DisplayName("Debe configurar streaming correctamente")
        void shouldConfigureStreamingCorrectly() {
            // Given
            String streamUrl = "https://twitch.tv/tournament";
            String platform = "Twitch";

            // When
            paidTournament.configureStreaming(streamUrl, platform);

            // Then
            assertEquals(streamUrl, paidTournament.getStreamUrl());
            assertEquals(platform, paidTournament.getStreamPlatform());
        }
    }
} 