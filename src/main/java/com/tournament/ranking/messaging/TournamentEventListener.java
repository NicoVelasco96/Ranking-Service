package com.tournament.ranking.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.ranking.service.IPlayerRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class TournamentEventListener {

    @Autowired
    private IPlayerRankingService rankingService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "tournament.finished.queue")
    public void onTournamentFinished(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long tournamentId = Long.valueOf(event.get("tournamentId").toString());
            Long championId = Long.valueOf(event.get("championId").toString());

            log.info("Evento TOURNAMENT_FINISHED recibido — torneo: {}, campeón: {}",
                    tournamentId, championId);

            rankingService.registerTournamentWin(championId);

        } catch (Exception e) {
            log.error("Error procesando evento TOURNAMENT_FINISHED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "tournament.started.queue")
    public void onTournamentStarted(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long tournamentId = Long.valueOf(event.get("tournamentId").toString());

            log.info("Evento TOURNAMENT_STARTED recibido — torneo: {}", tournamentId);

        } catch (Exception e) {
            log.error("Error procesando evento TOURNAMENT_STARTED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "match.finished.queue")
    public void onMatchFinished(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long winnerId = Long.valueOf(event.get("winnerId").toString());
            Long loserId = Long.valueOf(event.get("loserId").toString());

            log.info("Evento MATCH_FINISHED recibido — winner: {}, loser: {}", winnerId, loserId);

            rankingService.updateElo(winnerId, loserId);
        } catch (Exception e) {
            log.error("Error procesando evento MATCH_FINISHED: {}", e.getMessage());
        }
    }
}