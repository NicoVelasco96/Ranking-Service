package com.tournament.ranking.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class RankingDTO {

    @Data
    public static class RankingResponse {
        private Long playerId;
        private Integer eloRating;
        private Integer wins;
        private Integer losses;
        private Integer tournamentsPlayed;
        private Integer tournamentsWon;
        private Integer rank;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class EloUpdateRequest {
        private Long winnerId;
        private Long loserId;
    }
}