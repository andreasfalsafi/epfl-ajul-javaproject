package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.mcts.MctsPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGeneratorFactory;
import static org.junit.jupiter.api.Assertions.*;

class MctsPlayerTest {

    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void mctsPlayerComputesAMoveWithoutCrashing() {
        Game game = createGame2P();

        // On prépare un état initial
        ImmutableGameState initialState = ImmutableGameState.initial(game);
        MutableGameState mutableState = new MutableGameState(initialState);

        // On remplit les fabriques pour qu'il y ait des coups valides
        mutableState.fillFactories(new Random(42));

        // On instancie l'IA avec très peu d'itérations pour que le test soit instantané
        // (100 itérations suffisent pour vérifier que l'arbre grandit sans NullPointerException)
        MctsPlayer ai = new MctsPlayer(RandomGeneratorFactory.getDefault(), 100);

        // L'IA calcule le prochain coup
        short computedMove = ai.nextMove(mutableState);

        // On vérifie que le coup renvoyé n'est pas null
        assertNotNull(computedMove, "L'IA MCTS a renvoyé un coup null !");

        // Optionnel : On vérifie que le coup renvoyé fait bien partie des coups valides
        short[] validMoves = new short[216];
        int count = mutableState.validMoves(validMoves);

        boolean isMoveValid = false;
        short packedComputedMove = computedMove;
        for (int i = 0; i < count; i++) {
            if (validMoves[i] == packedComputedMove) {
                isMoveValid = true;
                break;
            }
        }

        assertTrue(isMoveValid, "Le coup calculé par l'IA ne fait pas partie des coups valides !");
    }
}