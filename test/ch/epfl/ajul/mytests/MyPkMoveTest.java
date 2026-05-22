package ch.epfl.ajul.mytests;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyPkMoveTest {

    @Test
    void packAndUnpackWorksCorrectly() {
        // On prend un cas au hasard : Fabrique 3, Couleur BLEU (B), Destination Ligne 2
        TileSource source = TileSource.ALL.get(3); // Suppose que c'est une fabrique
        TileKind.Colored color = TileKind.Colored.B;
        TileDestination dest = TileDestination.ALL.get(2); // Une destination valide au hasard
        short pkMove = PkMove.pack(source, color, dest);

        // On vérifie que le déballage redonne exactement les mêmes objets
        assertEquals(source, PkMove.source(pkMove));
        assertEquals(color, PkMove.color(pkMove));
        assertEquals(dest, PkMove.destination(pkMove));
    }

    @Test
    void packAndUnpackWorksCorrectly2() {
        TileSource source = TileSource.CENTER_AREA; // ex: TileSource.FACTORY_7
        TileKind.Colored color = TileKind.Colored.E; // ex: TileKind.Colored.C
        TileDestination destination = TileDestination.PATTERN_5; // ex: TileDestination.PATTERN_LINE_4

        short packed = PkMove.pack(source, color, destination);
        assertEquals(source, PkMove.source(packed));
        assertEquals(color, PkMove.color(packed));
        assertEquals(destination, PkMove.destination(packed));
    }

    @Test
    void packRespectsBitShiftingExample() {
        short packed = PkMove.pack(TileSource.FACTORY_7, TileKind.Colored.C, TileDestination.PATTERN_4);
        assertEquals(0b000000_011_010_0111, packed);
    }
}