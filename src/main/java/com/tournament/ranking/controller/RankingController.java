package com.tournament.ranking.controller;

import com.tournament.ranking.dto.RankingDTO;
import com.tournament.ranking.service.IPlayerRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/rankings")
public class RankingController {

    @Autowired
    private IPlayerRankingService rankingService;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<RankingDTO.RankingResponse>> getLeaderboard() {
        return ResponseEntity.ok(rankingService.getLeaderboard());
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<RankingDTO.RankingResponse> getByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(rankingService.getByPlayerId(playerId));
    }

    @PostMapping("/players/{playerId}")
    public ResponseEntity<Void> registerPlayer(@PathVariable Long playerId) {
        rankingService.registerPlayer(playerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/elo")
    public ResponseEntity<Void> updateElo(@RequestBody RankingDTO.EloUpdateRequest request) {
        rankingService.updateElo(request.getWinnerId(), request.getLoserId());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}