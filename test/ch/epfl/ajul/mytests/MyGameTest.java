package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MyGameTest {

    @Test
    void constructorThrowsOnInvalidPlayerCount() {
        Game.PlayerDescription p1 = new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);

        // Trop peu de joueurs (1)
        assertThrows(IllegalArgumentException.class, () -> new Game(List.of(p1)));

        // Valide (2)
        Game.PlayerDescription p2 = new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI);
        assertDoesNotThrow(() -> new Game(List.of(p1, p2)));
    }

    @Test
    void constructorThrowsOnInvalidPlayerIdOrder() {
        // Le premier joueur a l'ID P2 au lieu de P1
        Game.PlayerDescription wrongP1 = new Game.PlayerDescription(PlayerId.P2, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);
        Game.PlayerDescription p2 = new Game.PlayerDescription(PlayerId.P1, "Bob", Game.PlayerDescription.PlayerKind.AI);

        assertThrows(IllegalArgumentException.class, () -> new Game(List.of(wrongP1, p2)));
    }

    @Test
    void derivedCountsAndSizesAreCorrectForTwoPlayers() {
        Game.PlayerDescription p1 = new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);
        Game.PlayerDescription p2 = new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI);
        Game game = new Game(List.of(p1, p2));

        int expectedFactories = 2 * 2 + 1; // 2*n + 1 = 5
        assertEquals(5, game.factoriesCount());

        int expectedCentralSize = 3 * expectedFactories + 1; // 3*m + 1 = 16
        assertEquals(16, game.centralAreaMaxSize());
    }

    @Test
    void derivedCountsAndSizesAreCorrectForFourPlayers() {
        Game.PlayerDescription p1 = new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);
        Game.PlayerDescription p2 = new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI);
        Game.PlayerDescription p3 = new Game.PlayerDescription(PlayerId.P3, "Charlie", Game.PlayerDescription.PlayerKind.HUMAN);
        Game.PlayerDescription p4 = new Game.PlayerDescription(PlayerId.P4, "Dave", Game.PlayerDescription.PlayerKind.AI);

        Game game = new Game(List.of(p1, p2, p3, p4));

        int expectedFactories = 2 * 4 + 1; // 2*n + 1 = 9
        assertEquals(9, game.factoriesCount());

        int expectedCentralSize = 3 * expectedFactories + 1; // 3*m + 1 = 28
        assertEquals(28, game.centralAreaMaxSize());
    }
}