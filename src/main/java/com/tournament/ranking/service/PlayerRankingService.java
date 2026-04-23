package com.tournament.ranking.service;

import com.tournament.ranking.dto.RankingDTO;
import com.tournament.ranking.model.PlayerRanking;
import com.tournament.ranking.repository.IPlayerRankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PlayerRankingService implements IPlayerRankingService {

    private static final String LEADERBOARD_CACHE_KEY = "leaderboard";
    private static final long CACHE_TTL_MINUTES = 5;

    @Autowired
    private IPlayerRankingRepository playerRankingRepository;

    @Autowired
    private IELOService eloService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void registerPlayer(Long playerId) {
        if (playerRankingRepository.existsByPlayerId(playerId)) {
            log.info("Player {} ya tiene ranking registrado", playerId);
            return;
        }

        PlayerRanking ranking = PlayerRanking.builder()
                .playerId(playerId)
                .build();

        playerRankingRepository.save(ranking);
        log.info("Ranking creado para player {}", playerId);
    }

    @Override
    @Transactional
    public void updateElo(Long winnerId, Long loserId) {
        PlayerRanking winner = playerRankingRepository.findByPlayerId(winnerId)
                .orElseGet(() -> {
                    registerPlayer(winnerId);
                    return playerRankingRepository.findByPlayerId(winnerId).orElseThrow();
                });

        PlayerRanking loser = playerRankingRepository.findByPlayerId(loserId)
                .orElseGet(() -> {
                    registerPlayer(loserId);
                    return playerRankingRepository.findByPlayerId(loserId).orElseThrow();
                });

        int[] newRatings = eloService.calculateNewRatings(
                winner.getEloRating(), loser.getEloRating());

        winner.setEloRating(newRatings[0]);
        winner.setWins(winner.getWins() + 1);
        winner.setTournamentsPlayed(winner.getTournamentsPlayed() + 1);

        loser.setEloRating(newRatings[1]);
        loser.setLosses(loser.getLosses() + 1);
        loser.setTournamentsPlayed(loser.getTournamentsPlayed() + 1);

        playerRankingRepository.save(winner);
        playerRankingRepository.save(loser);

        invalidateLeaderboardCache();

        log.info("ELO actualizado — Winner: {} ({} pts), Loser: {} ({} pts)",
                winnerId, newRatings[0], loserId, newRatings[1]);
    }

    @Override
    @Transactional
    public void registerTournamentWin(Long playerId) {
        playerRankingRepository.findByPlayerId(playerId).ifPresent(ranking -> {
            ranking.setTournamentsWon(ranking.getTournamentsWon() + 1);
            playerRankingRepository.save(ranking);
            log.info("Tournament win registrado para player {}", playerId);
        });
    }

    @Override
    public RankingDTO.RankingResponse getByPlayerId(Long playerId) {
        PlayerRanking ranking = playerRankingRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Ranking no encontrado para player: " + playerId));
        return toResponse(ranking, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RankingDTO.RankingResponse> getLeaderboard() {
        List<RankingDTO.RankingResponse> cached =
                (List<RankingDTO.RankingResponse>) redisTemplate.opsForValue().get(LEADERBOARD_CACHE_KEY);

        if (cached != null) {
            log.info("Leaderboard obtenido desde cache Redis");
            return cached;
        }

        List<PlayerRanking> rankings = playerRankingRepository.findAllByOrderByEloRatingDesc();
        List<RankingDTO.RankingResponse> response = new java.util.ArrayList<>();

        for (int i = 0; i < rankings.size(); i++) {
            response.add(toResponse(rankings.get(i), i + 1));
        }

        redisTemplate.opsForValue().set(LEADERBOARD_CACHE_KEY, response, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Leaderboard guardado en cache Redis por {} minutos", CACHE_TTL_MINUTES);

        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void invalidateLeaderboardCache() {
        redisTemplate.delete(LEADERBOARD_CACHE_KEY);
        log.info("Cache del leaderboard invalidado");
    }

    private RankingDTO.RankingResponse toResponse(PlayerRanking r, Integer rank) {
        RankingDTO.RankingResponse res = new RankingDTO.RankingResponse();
        res.setPlayerId(r.getPlayerId());
        res.setEloRating(r.getEloRating());
        res.setWins(r.getWins());
        res.setLosses(r.getLosses());
        res.setTournamentsPlayed(r.getTournamentsPlayed());
        res.setTournamentsWon(r.getTournamentsWon());
        res.setRank(rank);
        res.setUpdatedAt(r.getUpdatedAt());
        return res;
    }
}