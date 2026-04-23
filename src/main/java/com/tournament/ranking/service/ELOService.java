package com.tournament.ranking.service;

import org.springframework.stereotype.Service;

@Service
public class ELOService implements IELOService{

    private static final int K_FACTOR = 32;
    private static final int BASE_RATING = 1000;

    @Override
    public int[] calculateNewRatings(int winnerRating, int loserRating) {
        double expectedWinner = expectedScore(winnerRating, loserRating);
        double expectedLoser = expectedScore(loserRating, winnerRating);

        int newWinnerRating = (int) Math.round(winnerRating + K_FACTOR * (1 - expectedWinner));
        int newLoserRating = (int) Math.round(loserRating + K_FACTOR * (0 - expectedLoser));

        // No bajar del rating base
        newLoserRating = Math.max(newLoserRating, BASE_RATING / 2);

        return new int[]{newWinnerRating, newLoserRating};
    }

    private double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
    }
}