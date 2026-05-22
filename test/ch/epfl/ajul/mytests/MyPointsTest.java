package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Points;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MyPointsTest {

//    @Test
//    void TotalFloorPenaltyHasRightValue() {
//        assertEquals(0xEB864210, Points.TOTAL_FLOOR_PENALTY);
//    }


    @Test
    void newWallTilePointsHandlesIsolatedAndConnectedCases() {
        // Totalement isolé : (1, 1) -> rapporte 1 (pas 2 !)
        assertEquals(1, Points.newWallTilePoints(1, 1));

        // Connecté seulement horizontalement
        assertEquals(4, Points.newWallTilePoints(4, 1));

        // Connecté seulement verticalement
        assertEquals(3, Points.newWallTilePoints(1, 3));

        // Connecté dans les deux sens (l'intersection compte deux fois indirectement : h + v)
        assertEquals(7, Points.newWallTilePoints(4, 3));
    }

    @Test
    void floorPenaltiesAccumulateCorrectlyUsingTheHexMaskRule() {
        // Vérification des pénalités individuelles
        assertEquals(1, Points.floorPenalty(0)); // 1ère tuile
        assertEquals(1, Points.floorPenalty(1)); // 2ème tuile
        assertEquals(2, Points.floorPenalty(2)); // 3ème tuile
        assertEquals(2, Points.floorPenalty(3)); // 4ème tuile
        assertEquals(2, Points.floorPenalty(4)); // 5ème tuile
        assertEquals(3, Points.floorPenalty(5)); // 6ème tuile
        assertEquals(3, Points.floorPenalty(6)); // 7ème tuile

        // Vérification des cumuls exacts
        assertEquals(0, Points.totalFloorPenalty(0), "0 tuile = 0 pt");
        assertEquals(1, Points.totalFloorPenalty(1), "1 tuile = 1 pt");
        assertEquals(2, Points.totalFloorPenalty(2), "2 tuiles = 1+1 = 2 pts");
        assertEquals(4, Points.totalFloorPenalty(3), "3 tuiles = 2+2 = 4 pts");
        assertEquals(6, Points.totalFloorPenalty(4), "4 tuiles = 4+2 = 6 pts");
        assertEquals(8, Points.totalFloorPenalty(5), "5 tuiles = 6+2 = 8 pts");
        assertEquals(11, Points.totalFloorPenalty(6), "6 tuiles = 8+3 = 11 pts");
        assertEquals(14, Points.totalFloorPenalty(7), "7 tuiles = 11+3 = 14 pts");
    }

}
