package ch.epfl.ajul.mytests;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gui.TileLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileLocationTest {

    @Test
    void tileLocationsAreCorrectlyInstantiated() {
        // Test d'un emplacement hors plateau
        TileLocation.OffBoard offBoard = new TileLocation.OffBoard(TileKind.Colored.A, 19);
        assertEquals(TileKind.Colored.A, offBoard.tileKind());
        assertEquals(19, offBoard.index());

        // Test d'un emplacement sur une fabrique
        TileLocation.OnSource onFactory = new TileLocation.OnSource(TileSource.FACTORY_1, 3);
        assertEquals(TileSource.FACTORY_1, onFactory.source());
        assertEquals(3, onFactory.index());

        // Test d'un emplacement sur le mur
        TileLocation.OnWall onWall = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C);
        assertEquals(PlayerId.P1, onWall.playerId());
        assertEquals(TileDestination.Pattern.PATTERN_2, onWall.line());
        assertEquals(TileKind.Colored.C, onWall.color());

        // Vérification du polymorphisme (scellé)
        assertTrue(offBoard instanceof TileLocation);
        assertTrue(onFactory instanceof TileLocation);
        assertTrue(onWall instanceof TileLocation);
    }
}