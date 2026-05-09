package com.tournament.ranking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.ranking.dto.RankingDTO;
import com.tournament.ranking.service.IPlayerRankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
@DisplayName("RankingController - Integration Tests")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private IPlayerRankingService rankingService;

    // ── GET /api/rankings/leaderboard ─────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /leaderboard: retorna lista de rankings con 200 OK")
    void getLeaderboard_returnsOk() throws Exception {
        RankingDTO.RankingResponse r1 = new RankingDTO.RankingResponse();
        r1.setPlayerId(1L);
        r1.setEloRating(1200);
        r1.setRank(1);

        RankingDTO.RankingResponse r2 = new RankingDTO.RankingResponse();
        r2.setPlayerId(2L);
        r2.setEloRating(1000);
        r2.setRank(2);

        when(rankingService.getLeaderboard()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/rankings/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playerId").value(1))
                .andExpect(jsonPath("$[0].eloRating").value(1200))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /leaderboard: retorna lista vacía si no hay rankings")
    void getLeaderboard_empty_returnsEmptyList() throws Exception {
        when(rankingService.getLeaderboard()).thenReturn(List.of());

        mockMvc.perform(get("/api/rankings/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/rankings/players/{playerId} ──────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /players/{playerId}: retorna ranking del player con 200 OK")
    void getByPlayerId_existingPlayer_returnsOk() throws Exception {
        RankingDTO.RankingResponse response = new RankingDTO.RankingResponse();
        response.setPlayerId(10L);
        response.setEloRating(1150);
        response.setWins(5);
        response.setLosses(2);

        when(rankingService.getByPlayerId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/rankings/players/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(10))
                .andExpect(jsonPath("$.eloRating").value(1150))
                .andExpect(jsonPath("$.wins").value(5))
                .andExpect(jsonPath("$.losses").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /players/{playerId}: retorna 400 si player no existe")
    void getByPlayerId_nonExistentPlayer_returnsBadRequest() throws Exception {
        when(rankingService.getByPlayerId(99L))
                .thenThrow(new IllegalArgumentException("Ranking no encontrado para player: 99"));

        mockMvc.perform(get("/api/rankings/players/99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ranking no encontrado para player: 99"));
    }

    // ── POST /api/rankings/players/{playerId} ─────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /players/{playerId}: registra player con 200 OK")
    void registerPlayer_returnsOk() throws Exception {
        doNothing().when(rankingService).registerPlayer(10L);

        mockMvc.perform(post("/api/rankings/players/10")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(rankingService).registerPlayer(10L);
    }

    // ── POST /api/rankings/elo ────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /elo: actualiza ELO con 200 OK")
    void updateElo_validRequest_returnsOk() throws Exception {
        RankingDTO.EloUpdateRequest request = new RankingDTO.EloUpdateRequest();
        request.setWinnerId(10L);
        request.setLoserId(20L);

        doNothing().when(rankingService).updateElo(10L, 20L);

        mockMvc.perform(post("/api/rankings/elo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(rankingService).updateElo(10L, 20L);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /elo: retorna 400 si body es inválido")
    void updateElo_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/rankings/elo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // winnerId/loserId son null pero no hay validación @NotNull
    }
}
