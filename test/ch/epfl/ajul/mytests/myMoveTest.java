package ch.epfl.ajul.mytests;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.packed.PkMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class myMoveTest {

    @Test
    void constructorThrowsOnNull() {
        // Le constructeur compact doit rejeter les null
        assertThrows(NullPointerException.class, () -> {
            new Move(null, TileKind.Colored.A, TileDestination.FLOOR);
        });
    }

    @Test
    void packAndUnpackObjectsWorks() {
        TileSource source = TileSource.ALL.get(0);
        TileKind.Colored color = TileKind.Colored.C;
        TileDestination dest = TileDestination.FLOOR;

        Move move = new Move(source, color, dest);
        short packed = move.packed();
        Move unpackedMove = Move.ofPacked(packed);

        // Comme Move est un Record, le equals() est généré automatiquement
        // et on peut comparer directement les deux objets !
        assertEquals(move, unpackedMove);
    }

    @Test
    void moveConstructorThrowsOnNullArguments() {
        // Remplacer par des instances valides de tes énumérations de l'étape 1
        TileSource validSource = null;
        TileKind.Colored validColor = null;
        TileDestination validDest = null;

        assertThrows(NullPointerException.class, () -> new Move(null, validColor, validDest));
        assertThrows(NullPointerException.class, () -> new Move(validSource, null, validDest));
        assertThrows(NullPointerException.class, () -> new Move(validSource, validColor, null));
    }


    @Test
    void maxMovesIsCorrect() {
        // 9 fabriques * 4 tuiles * 6 destinations = 216
        assertEquals(216, Move.MAX_MOVES);
    }

    @Test
    void packedAndOfPackedAreConsistent() {
        Move originalMove = new Move(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR);
        short packedMove = originalMove.packed();
        Move reconstructedMove = Move.ofPacked(packedMove);

        assertEquals(originalMove, reconstructedMove);

    }
}

