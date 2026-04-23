package com.tournament.ranking.service;

import com.tournament.ranking.dto.RankingDTO;

import java.util.List;

public interface IPlayerRankingService {
    public RankingDTO.RankingResponse getByPlayerId(Long playerId);
    public List<RankingDTO.RankingResponse> getLeaderboard();
    public void updateElo(Long winnerId, Long loserId);
    public void registerPlayer(Long playerId);
    public void registerTournamentWin(Long playerId);
}
