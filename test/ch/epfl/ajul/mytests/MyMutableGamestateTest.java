package ch.epfl.ajul.mytests;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyMutableGamestateTest {

    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void fillFactoriesExtractsCorrectNumberOfTilesFromBag() {
        Game game = createGame2P();
        ImmutableGameState initialState = ImmutableGameState.initial(game);

        // On attache un observateur vide
        MutableGameState mutableState = new MutableGameState(initialState, PointsObserver.EMPTY);

        // Au début, le sac contient 100 tuiles (20 de chaque couleur)
        assertEquals(100, PkTileSet.size(mutableState.pkTileBag()));

        // On remplit les fabriques (5 fabriques de 4 tuiles pour 2 joueurs = 20 tuiles nécessaires)
        // On utilise Random avec une graine fixe pour éviter les comportements imprévisibles en test
        mutableState.fillFactories(new Random(42));

        // 1. Le sac doit avoir perdu exactement 20 tuiles
        assertEquals(80, PkTileSet.size(mutableState.pkTileBag()), "Le sac aurait dû perdre 20 tuiles.");

        // 2. Les 5 fabriques doivent contenir exactement 4 tuiles chacune
        int[] sources = mutableState.pkTileSources().toArray();
        for (int i = 1; i <= 5; i++) {
            assertEquals(4, PkTileSet.size(sources[i]), "La fabrique " + i + " doit contenir 4 tuiles.");
        }

        // 3. Le centre (source 0) ne doit PAS avoir été rempli avec des tuiles du sac
        assertEquals(1, PkTileSet.size(sources[0]), "Le centre ne doit contenir que le marqueur de 1er joueur.");
        assertTrue(PkTileSet.countOf(sources[0], TileKind.FIRST_PLAYER_MARKER)>0);
    }

    @Test
    void registerMoveDistributesTilesCorrectlyAndChangesPlayer() {
        Game game = createGame2P();
        int[] states = new int[8];
        int[] sources = new int[6];

        // Fabrique 1 contient : 3 tuiles A et 1 tuile B
        int factory1 = PkTileSet.of(3, TileKind.Colored.A);
        factory1 = PkTileSet.add(factory1, TileKind.Colored.B);
        sources[1] = factory1;

        // Centre contient juste le marqueur
        sources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        ImmutableGameState initialState = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), 0, ImmutableIntArray.copyOf(states), PlayerId.P1
        );
        MutableGameState mutableState = new MutableGameState(initialState);

        // P1 décide de prendre la couleur A dans la Fabrique 1, pour la mettre sur la Ligne 2 (capacité 2)
        // Attention : Adapte la création de ton PkMove selon ton code (ex: Move.pkMove(source, couleur, destination))
        short move = PkMove.pack(TileSource.ALL.get(1), TileKind.Colored.A, TileDestination.Pattern.PATTERN_2);

        mutableState.registerMove(move);

        // --- Vérifications ---

        // 1. Le joueur courant doit maintenant être P2
        assertEquals(PlayerId.P2, mutableState.currentPlayerId());

        // 2. La Fabrique 1 doit être VIDES
        assertEquals(0, mutableState.pkTileSources().get(1));

        // 3. Le centre doit avoir reçu la tuile B restante de la Fabrique 1
        int center = mutableState.pkTileSources().get(0);
        assertEquals(1, PkTileSet.countOf(center, TileKind.Colored.B), "La tuile B restante doit aller au centre");

        // 4. Le plateau de P1 doit avoir reçu les tuiles A
        int p1Patterns = PkPlayerStates.pkPatterns(mutableState.pkPlayerStates(), PlayerId.P1);
        assertEquals(2, PkPatterns.size(p1Patterns, TileDestination.Pattern.PATTERN_2), "2 tuiles A sur la ligne 2");

        // 5. La 3ème tuile A était en surplus (capacité 2), elle a dû tomber sur le plancher !
        int p1Floor = PkPlayerStates.pkFloor(mutableState.pkPlayerStates(), PlayerId.P1);
        assertEquals(1, PkFloor.size(p1Floor), "Le surplus doit aller sur le plancher");
        assertEquals(TileKind.Colored.A, PkFloor.tileAt(p1Floor, 0));
    }

    @Test
    void endRoundMovesTilesToWallScoresPointsAndEmptiesFloor() {
        Game game = createGame2P();
        int[] states = new int[8];

        // On donne 10 points de base à P1 pour éviter qu'il ne bloque à 0
        PkPlayerStates.addPoints(states, PlayerId.P1, 10);

        // P1 a sa Ligne 1 pleine avec du A
        int patterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(states, PlayerId.P1, patterns);

        // P1 a 2 tuiles B sur son plancher (pénalité de -2 points au total)
        int floor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(states, PlayerId.P1, floor);

        int[] sources = new int[6]; // Sources vides, la manche est finie

        ImmutableGameState initialState = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), 0, ImmutableIntArray.copyOf(states), PlayerId.P1
        );
        MutableGameState mutableState = new MutableGameState(initialState);

        // Exécution de la fin de manche
        mutableState.endRound();

        // --- Vérifications ---

        ReadOnlyIntArray newStates = mutableState.pkPlayerStates();

        // 1. La Ligne 1 doit être vidée
        assertEquals(PkPatterns.EMPTY, PkPlayerStates.pkPatterns(newStates, PlayerId.P1));

        // 2. Le mur doit contenir la tuile A sur la ligne 1
        int newWall = PkPlayerStates.pkWall(newStates, PlayerId.P1);
        assertTrue(PkWall.hasTileAt(newWall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));

        // 3. Le plancher doit être vidé
        assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(newStates, PlayerId.P1));

        // 4. Les points : 10 (base) + 1 (tuile isolée sur le mur) - 2 (plancher) = 9 points
        assertEquals(9, PkPlayerStates.points(newStates, PlayerId.P1));
    }
    @Test
    void endGameCalculatesBonusPointsCorrectly() {
        Game game = createGame2P();
        int[] states = new int[8];

        int wall = PkWall.EMPTY;
        // On remplit entièrement la LIGNE 1 du mur de P1 (2 points bonus)
        wall = PkIntSet32.add(wall, 0); // A
        wall = PkIntSet32.add(wall, 1); // B
        wall = PkIntSet32.add(wall, 2); // C
        wall = PkIntSet32.add(wall, 3); // D
        wall = PkIntSet32.add(wall, 4); // E

        PkPlayerStates.setPkWall(states, PlayerId.P1, wall);
        // Le score de base de P1 est de 0.

        ImmutableGameState initialState = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(new int[6]), 0, ImmutableIntArray.copyOf(states), PlayerId.P1
        );
        MutableGameState mutableState = new MutableGameState(initialState);

        // Exécution de la fin de partie
        mutableState.endGame();

        // P1 doit avoir gagné 2 points pour sa ligne complète
        assertEquals(2, PkPlayerStates.points(mutableState.pkPlayerStates(), PlayerId.P1));
    }

}
