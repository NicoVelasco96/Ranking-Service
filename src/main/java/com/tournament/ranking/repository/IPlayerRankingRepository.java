package com.tournament.ranking.repository;

import com.tournament.ranking.model.PlayerRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPlayerRankingRepository extends JpaRepository<PlayerRanking, Long> {
    Optional<PlayerRanking> findByPlayerId(Long playerId);
    boolean existsByPlayerId(Long playerId);
    List<PlayerRanking> findAllByOrderByEloRatingDesc();
}
