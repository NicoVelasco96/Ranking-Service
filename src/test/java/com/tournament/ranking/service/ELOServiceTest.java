package com.tournament.ranking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ELOService - Unit Tests")
class ELOServiceTest {

    private ELOService eloService;

    @BeforeEach
    void setUp() {
        eloService = new ELOService();
    }

    @Test
    @DisplayName("Jugadores con rating igual: winner sube y loser baja simétricamente")
    void calculateNewRatings_equalRatings_symmetricChange() {
        int[] result = eloService.calculateNewRatings(1000, 1000);

        assertEquals(1016, result[0], "Winner debería subir 16 puntos");
        assertEquals(984, result[1], "Loser debería bajar 16 puntos");
    }

    @Test
    @DisplayName("Winner con rating mucho mayor: sube poco")
    void calculateNewRatings_strongWinner_smallGain() {
        int[] result = eloService.calculateNewRatings(2000, 1000);

        assertTrue(result[0] > 2000, "Winner debería subir");
        assertTrue(result[0] - 2000 < 5, "Winner muy fuerte debería subir muy poco");
    }

    @Test
    @DisplayName("Winner con rating mucho menor: sube mucho (upset)")
    void calculateNewRatings_weakWinnerUpset_largeGain() {
        int[] result = eloService.calculateNewRatings(1000, 2000);

        assertTrue(result[0] > 1000, "Winner debería subir");
        assertTrue(result[0] - 1000 > 25, "Upset debería dar muchos puntos al winner");
    }

    @Test
    @DisplayName("Loser nunca baja del rating mínimo (500)")
    void calculateNewRatings_loserNeverBelowMinimum() {
        int[] result = eloService.calculateNewRatings(2000, 501);

        assertTrue(result[1] >= 500, "Loser no puede bajar de 500");
    }

    @Test
    @DisplayName("Retorna array de exactamente 2 elementos")
    void calculateNewRatings_returnsArrayOfTwo() {
        int[] result = eloService.calculateNewRatings(1000, 1000);

        assertNotNull(result);
        assertEquals(2, result.length);
    }

    @Test
    @DisplayName("Winner siempre termina con más rating que loser si empezaban igual")
    void calculateNewRatings_equalStart_winnerAlwaysHigher() {
        int[] result = eloService.calculateNewRatings(1000, 1000);

        assertTrue(result[0] > result[1], "Winner debe quedar con más rating");
    }

    @Test
    @DisplayName("K-Factor 32: cambio máximo posible no supera 32 puntos")
    void calculateNewRatings_maxChangeDoesNotExceedKFactor() {
        int[] result = eloService.calculateNewRatings(500, 3000);

        assertTrue(result[0] - 500 <= 32, "El cambio no puede superar el K-Factor");
    }
}
