package com.tournament.ranking.service;

public interface IELOService {
    public int[] calculateNewRatings(int winnerRating, int loserRating);
}
