package com.tournament.ranking.service;

import com.tournament.ranking.dto.RankingDTO;
import com.tournament.ranking.model.PlayerRanking;
import com.tournament.ranking.repository.IPlayerRankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerRankingService - Unit Tests")
class PlayerRankingServiceTest {

    @Mock
    private IPlayerRankingRepository playerRankingRepository;

    @Mock
    private IELOService eloService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PlayerRankingService playerRankingService;

    private PlayerRanking winner;
    private PlayerRanking loser;

    @BeforeEach
    void setUp() {
        winner = PlayerRanking.builder()
                .id(1L).playerId(10L).eloRating(1000)
                .wins(0).losses(0).tournamentsPlayed(0).tournamentsWon(0)
                .build();

        loser = PlayerRanking.builder()
                .id(2L).playerId(20L).eloRating(1000)
                .wins(0).losses(0).tournamentsPlayed(0).tournamentsWon(0)
                .build();
    }

    // ── registerPlayer ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerPlayer: crea ranking nuevo si no existe")
    void registerPlayer_newPlayer_createsRanking() {
        when(playerRankingRepository.existsByPlayerId(10L)).thenReturn(false);

        playerRankingService.registerPlayer(10L);

        verify(playerRankingRepository).save(argThat(r ->
                r.getPlayerId().equals(10L) &&
                r.getEloRating().equals(1000) &&
                r.getWins().equals(0)
        ));
    }

    @Test
    @DisplayName("registerPlayer: no crea ranking si ya existe")
    void registerPlayer_existingPlayer_doesNothing() {
        when(playerRankingRepository.existsByPlayerId(10L)).thenReturn(true);

        playerRankingService.registerPlayer(10L);

        verify(playerRankingRepository, never()).save(any());
    }

    // ── updateElo ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateElo: actualiza ratings de winner y loser correctamente")
    void updateElo_existingPlayers_updatesRatings() {
        when(playerRankingRepository.findByPlayerId(10L)).thenReturn(Optional.of(winner));
        when(playerRankingRepository.findByPlayerId(20L)).thenReturn(Optional.of(loser));
        when(eloService.calculateNewRatings(1000, 1000)).thenReturn(new int[]{1016, 984});
        when(redisTemplate.delete(anyString())).thenReturn(true);

        playerRankingService.updateElo(10L, 20L);

        assertEquals(1016, winner.getEloRating());
        assertEquals(1, winner.getWins());
        assertEquals(1, winner.getTournamentsPlayed());
        assertEquals(984, loser.getEloRating());
        assertEquals(1, loser.getLosses());
        assertEquals(1, loser.getTournamentsPlayed());

        verify(playerRankingRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("updateElo: registra automáticamente player si no existe")
    void updateElo_newPlayer_registersAndUpdates() {
        when(playerRankingRepository.findByPlayerId(10L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        when(playerRankingRepository.findByPlayerId(20L)).thenReturn(Optional.of(loser));
        when(playerRankingRepository.existsByPlayerId(10L)).thenReturn(false);
        when(eloService.calculateNewRatings(anyInt(), anyInt())).thenReturn(new int[]{1016, 984});
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> playerRankingService.updateElo(10L, 20L));

        verify(playerRankingRepository).existsByPlayerId(10L);
    }

    @Test
    @DisplayName("updateElo: invalida cache de leaderboard")
    void updateElo_always_invalidatesCache() {
        when(playerRankingRepository.findByPlayerId(10L)).thenReturn(Optional.of(winner));
        when(playerRankingRepository.findByPlayerId(20L)).thenReturn(Optional.of(loser));
        when(eloService.calculateNewRatings(anyInt(), anyInt())).thenReturn(new int[]{1016, 984});
        when(redisTemplate.delete(anyString())).thenReturn(true);

        playerRankingService.updateElo(10L, 20L);

        verify(redisTemplate).delete("leaderboard");
    }

    // ── registerTournamentWin ─────────────────────────────────────────────────

    @Test
    @DisplayName("registerTournamentWin: incrementa tournamentsWon")
    void registerTournamentWin_existingPlayer_incrementsWins() {
        when(playerRankingRepository.findByPlayerId(10L)).thenReturn(Optional.of(winner));

        playerRankingService.registerTournamentWin(10L);

        assertEquals(1, winner.getTournamentsWon());
        verify(playerRankingRepository).save(winner);
    }

    @Test
    @DisplayName("registerTournamentWin: no hace nada si player no existe")
    void registerTournamentWin_nonExistentPlayer_doesNothing() {
        when(playerRankingRepository.findByPlayerId(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> playerRankingService.registerTournamentWin(99L));

        verify(playerRankingRepository, never()).save(any());
    }

    // ── getByPlayerId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByPlayerId: retorna RankingResponse correctamente")
    void getByPlayerId_existingPlayer_returnsResponse() {
        when(playerRankingRepository.findByPlayerId(10L)).thenReturn(Optional.of(winner));

        RankingDTO.RankingResponse response = playerRankingService.getByPlayerId(10L);

        assertNotNull(response);
        assertEquals(10L, response.getPlayerId());
        assertEquals(1000, response.getEloRating());
    }

    @Test
    @DisplayName("getByPlayerId: lanza excepción si player no existe")
    void getByPlayerId_nonExistentPlayer_throwsException() {
        when(playerRankingRepository.findByPlayerId(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> playerRankingService.getByPlayerId(99L));
    }

    // ── getLeaderboard ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLeaderboard: retorna desde cache si existe")
    void getLeaderboard_cacheHit_returnsCached() {
        List<RankingDTO.RankingResponse> cached = List.of(new RankingDTO.RankingResponse());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("leaderboard")).thenReturn(cached);

        List<RankingDTO.RankingResponse> result = playerRankingService.getLeaderboard();

        assertEquals(cached, result);
        verify(playerRankingRepository, never()).findAllByOrderByEloRatingDesc();
    }

    @Test
    @DisplayName("getLeaderboard: consulta DB y guarda en cache si no hay cache")
    void getLeaderboard_cacheMiss_queriesDBAndCaches() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("leaderboard")).thenReturn(null);
        when(playerRankingRepository.findAllByOrderByEloRatingDesc())
                .thenReturn(List.of(winner, loser));

        List<RankingDTO.RankingResponse> result = playerRankingService.getLeaderboard();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getRank());
        assertEquals(2, result.get(1).getRank());

        verify(valueOperations).set(eq("leaderboard"), any(), eq(5L), eq(TimeUnit.MINUTES));
    }
}
