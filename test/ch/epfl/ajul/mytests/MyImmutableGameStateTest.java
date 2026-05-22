package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class MyImmutableGameStateTest {

    // Méthode utilitaire pour créer une partie à 2 joueurs
    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void initialGameStateIsCorrectlyPopulated() {
        Game game = createGame2P();
        ImmutableGameState state = ImmutableGameState.initial(game);

        // 1. Le joueur courant doit être P1
        assertEquals(PlayerId.P1, state.currentPlayerId());

        // 2. Le sac doit contenir 20 tuiles de chaque couleur (A, B, C, D, E)
        int bag = state.pkTileBag();
        assertEquals(20, PkTileSet.countOf(bag, TileKind.Colored.A));
        assertEquals(20, PkTileSet.countOf(bag, TileKind.Colored.B));
        assertEquals(0, PkTileSet.countOf(bag, TileKind.FIRST_PLAYER_MARKER), "Le marqueur ne va JAMAIS dans le sac");

        // 3. La zone centrale (source 0) doit contenir uniquement le marqueur
        int center = state.pkTileSources().get(0);
        assertEquals(1, PkTileSet.countOf(center, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(1, PkTileSet.size(center));

        // 4. Les fabriques (sources 1 à 5) doivent être vides au départ
        assertEquals(0, state.pkTileSources().get(1));
        assertEquals(0, state.pkTileSources().get(5));
    }
}