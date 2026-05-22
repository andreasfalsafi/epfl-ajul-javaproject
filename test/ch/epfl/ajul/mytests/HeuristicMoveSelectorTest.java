package ch.epfl.ajul.mytests;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class HeuristicMoveSelectorTest {

    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void selectMovePrioritizesMovesThatPerfectlyFillAPatternLine() {
        Game game = createGame2P();
        int[] states = new int[8];

        // P1 a 2 tuiles A sur sa ligne 3 (capacité 3). Il manque EXACTEMENT 1 tuile A pour la remplir.
        int patterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(states, PlayerId.P1, patterns);

        // P1 a sa ligne 4 vide (capacité 4).

        int[] sources = new int[6];
        // La fabrique 1 contient exactement 1 tuile A et 1 tuile B
        int factory1 = PkTileSet.of(1, TileKind.Colored.A);
        factory1 = PkTileSet.union(factory1, PkTileSet.of( 1, TileKind.Colored.B));
        sources[1] = factory1;

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), (1 << 1), ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        // On génère 3 coups à la main pour le test (adapte la signature de PkMove)
        short movePerfectFill = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_3); // Remplit ligne 3
        short movePartialFill = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.Pattern.PATTERN_4); // Remplit partiel ligne 4
        short moveFloor = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.Floor.FLOOR); // Jette sur le plancher

        short[] validMoves = new short[] { moveFloor, movePartialFill, movePerfectFill };

        // L'heuristique DOIT choisir le movePerfectFill, peu importe le Random,
        // car c'est le seul qui remplit totalement une ligne sans déborder !
        int selectedIndex = HeuristicMoveSelector.selectMove(new Random(), state, validMoves, 3);

        assertEquals(2, selectedIndex, "L'heuristique doit donner la priorité absolue aux coups qui remplissent parfaitement une ligne.");
    }
}
