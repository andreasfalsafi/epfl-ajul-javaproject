package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RankComputerTest {

    private Game createGame4P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P3, "Charlie", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P4, "Dave", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void playersRankHandlesTiesCorrectlyLikeTheExample() {
        Game game = createGame4P();
        int[] states = new int[16]; // 4 joueurs * 4 ints

        // --- Joueur 1 (Alice) : 45 pts, 3 lignes ---
        PkPlayerStates.addPoints(states, PlayerId.P1, 45);
        int wallP1 = PkWall.EMPTY;
        wallP1 = PkIntSet32.add(wallP1, 0); wallP1 = PkIntSet32.add(wallP1, 1); wallP1 = PkIntSet32.add(wallP1, 2); wallP1 = PkIntSet32.add(wallP1, 3); wallP1 = PkIntSet32.add(wallP1, 4); // Ligne 1
        wallP1 = PkIntSet32.add(wallP1, 5); wallP1 = PkIntSet32.add(wallP1, 6); wallP1 = PkIntSet32.add(wallP1, 7); wallP1 = PkIntSet32.add(wallP1, 8); wallP1 = PkIntSet32.add(wallP1, 9); // Ligne 2
        wallP1 = PkIntSet32.add(wallP1, 10); wallP1 = PkIntSet32.add(wallP1, 11); wallP1 = PkIntSet32.add(wallP1, 12); wallP1 = PkIntSet32.add(wallP1, 13); wallP1 = PkIntSet32.add(wallP1, 14); // Ligne 3
        PkPlayerStates.setPkWall(states, PlayerId.P1, wallP1);

        // --- Joueur 2 (Bob) : 55 pts, 2 lignes ---
        PkPlayerStates.addPoints(states, PlayerId.P2, 55);
        int wallP2 = PkWall.EMPTY;
        wallP2 = PkIntSet32.add(wallP2, 0); wallP2 = PkIntSet32.add(wallP2, 1); wallP2 = PkIntSet32.add(wallP2, 2); wallP2 = PkIntSet32.add(wallP2, 3); wallP2 = PkIntSet32.add(wallP2, 4); // Ligne 1
        wallP2 = PkIntSet32.add(wallP2, 5); wallP2 = PkIntSet32.add(wallP2, 6); wallP2 = PkIntSet32.add(wallP2, 7); wallP2 = PkIntSet32.add(wallP2, 8); wallP2 = PkIntSet32.add(wallP2, 9); // Ligne 2
        PkPlayerStates.setPkWall(states, PlayerId.P2, wallP2);

        // --- Joueur 3 (Charlie) : 55 pts, 1 ligne ---
        PkPlayerStates.addPoints(states, PlayerId.P3, 55);
        int wallP3 = PkWall.EMPTY;
        wallP3 = PkIntSet32.add(wallP3, 0); wallP3 = PkIntSet32.add(wallP3, 1); wallP3 = PkIntSet32.add(wallP3, 2); wallP3 = PkIntSet32.add(wallP3, 3); wallP3 = PkIntSet32.add(wallP3, 4); // Ligne 1
        PkPlayerStates.setPkWall(states, PlayerId.P3, wallP3);

        // --- Joueur 4 (Dave) : 55 pts, 2 lignes ---
        PkPlayerStates.addPoints(states, PlayerId.P4, 55);
        PkPlayerStates.setPkWall(states, PlayerId.P4, wallP2); // Même mur que P2 !

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(new int[6]), 0, ImmutableIntArray.copyOf(states), PlayerId.P1
        );
        // --- Exécution du classement ---
        int[] ranks = new int[4];
        RankComputer.playersRank(state, ranks);

        // Résultats attendus selon l'énoncé :
        // P2 et P4 sont ex aequo à la première place (Rang 0)
        // P3 est 3ème car 55pts mais seulement 1 ligne (Rang 2, l'index 1 est "sauté")
        // P1 est dernier avec 45pts (Rang 3)

        assertEquals(3, ranks[0], "Alice (P1) a le moins de points, elle doit être rang 3");
        assertEquals(0, ranks[1], "Bob (P2) est vainqueur (Rang 0)");
        assertEquals(2, ranks[2], "Charlie (P3) est 3ème car il a une ligne de moins que Bob/Dave (Rang 2)");
        assertEquals(0, ranks[3], "Dave (P4) est ex aequo avec Bob (Rang 0)");
    }
}