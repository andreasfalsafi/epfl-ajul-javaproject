package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyReadOnlyGameStateTest {

    // Note: Pour tester des méthodes par défaut, on utilise l'enregistrement ImmutableGameState qui implémente l'interface.

    @Test
    void isRoundOverReturnsTrueOnlyWhenNoColoredTilesLeftInSources() {
        List<Game.PlayerDescription> twoPlayerGame=new ArrayList<>(2);
        twoPlayerGame.add(new Game.PlayerDescription(PlayerId.P1, "bloublou", Game.PlayerDescription.PlayerKind.HUMAN));
        twoPlayerGame.add(new Game.PlayerDescription(PlayerId.P2, "blabla", Game.PlayerDescription.PlayerKind.HUMAN));
        Game game = new Game(twoPlayerGame); // On réutilise le mock de 2 joueurs
//        ImmutableGameState initialState = ImmutableGameState.initial(game);

        // Au début, il n'y a que le marqueur au centre, et les fabriques sont vides.
        // Donc il n'y a AUCUNE tuile colorée dans les sources. La manche est techniquement "terminée" (ou plutôt prête à être remplie).
        assertTrue(ImmutableGameState.initial(game).isRoundOver(), "S'il n'y a que le marqueur de premier joueur, la manche est terminée");
    }

    @Test
    void isGameOverRequiresBothRoundOverAndFullWallRow() {
        Game game = createGame2P();

        // --- 1. Préparation des sources (Pour isRoundOver) ---

        int[] activeSources = new int[6];
        // Une tuile colorée dans la fabrique 1 -> La manche est TOUJOURS EN COURS
        activeSources[1] = PkTileSet.of(1, TileKind.Colored.A);
        // La fabrique 1 est la seule à avoir une tuile colorée, donc c'est la seule source unique (bit 1)
        int activeUniqueSources = (1 << 1);

        int[] emptySources = new int[6];
        // Le centre a le marqueur, mais AUCUNE tuile colorée -> La manche est TERMINÉE
        emptySources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        // Attention : Une source unique DOIT contenir au moins une tuile colorée.
        // Donc ici, il n'y a aucune source unique ! (masque = 0)
        int emptyUniqueSources = 0;

        // --- 2. Préparation des états des joueurs (Pour hasFullRow) ---

        int[] normalStates = new int[8];
        // Les murs sont vides -> PERSONNE N'A DE LIGNE

        int[] winningStates = new int[8];
        int fullWall = PkWall.EMPTY;
        // On remplit entièrement la première ligne du mur (index 0, 1, 2, 3, 4)
        fullWall = PkIntSet32.add(fullWall, 0);
        fullWall = PkIntSet32.add(fullWall, 1);
        fullWall = PkIntSet32.add(fullWall, 2);
        fullWall = PkIntSet32.add(fullWall, 3);
        fullWall = PkIntSet32.add(fullWall, 4);
        PkPlayerStates.setPkWall(winningStates, PlayerId.P1, fullWall);
        // -> P1 A UNE LIGNE COMPLÈTE

        // --- 3. Les 4 Scénarios ---

        // CAS 1 : Manche en cours, pas de ligne -> FALSE
        ImmutableGameState state1 = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(activeSources), activeUniqueSources, ImmutableIntArray.copyOf(normalStates), PlayerId.P1
        );
        assertFalse(state1.isGameOver(), "Cas 1 : La partie continue (manche en cours, pas de ligne complète).");

        // CAS 2 : Manche terminée, mais pas de ligne -> FALSE
        ImmutableGameState state2 = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(emptySources), emptyUniqueSources, ImmutableIntArray.copyOf(normalStates), PlayerId.P1
        );
        assertFalse(state2.isGameOver(), "Cas 2 : La manche est finie, mais personne n'a complété de ligne. On prépare la manche suivante !");

        // CAS 3 : Ligne pleine, mais manche en cours -> FALSE (Le piège !)
        ImmutableGameState state3 = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(activeSources), activeUniqueSources, ImmutableIntArray.copyOf(winningStates), PlayerId.P1
        );
        assertFalse(state3.isGameOver(), "Cas 3 : P1 a une ligne complète, mais il reste des tuiles dans les fabriques. La manche doit se terminer !");

        // CAS 4 : Manche terminée ET ligne pleine -> TRUE (La fin du jeu)
        ImmutableGameState state4 = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(emptySources), emptyUniqueSources, ImmutableIntArray.copyOf(winningStates), PlayerId.P1
        );
        assertTrue(state4.isGameOver(), "Cas 4 : La manche est finie ET P1 a une ligne complète. La partie est officiellement terminée.");
    }

    @Test
    void pkDiscardedTilesCalculatesCorrectlyBySubtraction() {
        List<Game.PlayerDescription> twoPlayerGame=new ArrayList<>(2);
        twoPlayerGame.add(new Game.PlayerDescription(PlayerId.P1, "bloublou", Game.PlayerDescription.PlayerKind.HUMAN));
        twoPlayerGame.add(new Game.PlayerDescription(PlayerId.P2, "blabla", Game.PlayerDescription.PlayerKind.HUMAN));
        Game game = new Game(twoPlayerGame); // On réutilise le mock de 2 joueurs

        // Pour ce test, on va tricher un peu en appelant le constructeur de ImmutableGameState
        // avec des données fausses mais contrôlées, pour voir si la méthode calcule bien la différence.

        // Imagine un sac contenant 10 tuiles A
        int fakeBag = PkTileSet.of(10, TileKind.Colored.A);

        // Des sources totalement vides
        ImmutableIntArray fakeSources = ImmutableIntArray.copyOf(new int[6]); // 6 sources de 0

        // Des plateaux vides
        ImmutableIntArray fakePlayers = ImmutableIntArray.copyOf(new int[8]);

        // On construit l'état
        ImmutableGameState customState = new ImmutableGameState(
                game, fakeBag, fakeSources, 0, fakePlayers, PlayerId.P1
        );

        // Il devrait y avoir 20 tuiles A en tout dans le jeu.
        // S'il y en a 10 dans le sac, et 0 sur le plateau/sources, alors il DOIT y en avoir 10 dans la défausse.
        // De même, les 20 tuiles B, C, D, E doivent être dans la défausse, ainsi que le marqueur de premier joueur.
        int discarded = customState.pkDiscardedTiles();

        assertEquals(10, PkTileSet.countOf(discarded, TileKind.Colored.A));
        assertEquals(20, PkTileSet.countOf(discarded, TileKind.Colored.B));
        assertEquals(1, PkTileSet.countOf(discarded, TileKind.FIRST_PLAYER_MARKER));
    }

    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void validMovesRespectsCapacityAndColorConstraints() {
        Game game = createGame2P();

        // 1. On prépare un état de joueur 1 très spécifique
        int[] states = new int[8];
        int patterns = PkPatterns.EMPTY;

        // Ligne 1 : PLEINE avec la couleur A (capacité 1)
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        // Ligne 2 : Partiellement remplie avec la couleur B (1 tuile sur 2)
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.B);

        PkPlayerStates.setPkPatterns(states, PlayerId.P1, patterns);

        // 2. On prépare des sources très simples
        int[] sources = new int[6];
        // Fabrique 1 contient : 1 tuile A et 1 tuile C
        int factory1 = PkTileSet.of(1, TileKind.Colored.A);
        factory1 = PkTileSet.add(factory1, TileKind.Colored.C);
        sources[1] = factory1;
        // Toutes les autres sources (y compris le centre) sont vides pour ce test.

        int pkUniqueTileSources= 0;
        pkUniqueTileSources=PkIntSet32.add(pkUniqueTileSources,1 );


        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), pkUniqueTileSources,  ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        // 3. On calcule les coups
        short[] moves = new short[216]; // La taille max selon l'étape 2
        int moveCount = state.validMoves(moves);

        // 4. Combien de coups attend-on ?
        // Depuis la Fabrique 1, on peut choisir la couleur A ou la couleur C.

        // Si on choisit la couleur A :
        // - Ligne 1 ? NON (Pleine)
        // - Ligne 2 ? NON (Contient du B)
        // - Lignes 3, 4, 5 ? OUI
        // - Plancher ? OUI (Toujours valide)
        // => 4 coups possibles avec la couleur A

        // Si on choisit la couleur C :
        // - Ligne 1 ? NON (Pleine)
        // - Ligne 2 ? NON (Contient du B)
        // - Lignes 3, 4, 5 ? OUI
        // - Plancher ? OUI
        // => 4 coups possibles avec la couleur C

        // Total attendu : 8 coups !
        assertEquals(8, moveCount, "Le nombre de coups valides générés est incorrect par rapport aux contraintes du plateau.");
    }

    @Test
    void validMovesRespectsWallConstraints() {
        Game game = createGame2P();
        int[] states = new int[8];

        // Le mur de P1 a déjà une tuile bleue (couleur D) sur la ligne 3.
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_3, TileKind.Colored.D);
        PkPlayerStates.setPkWall(states, PlayerId.P1, wall);

        // Les lignes de motif sont VIDES.
        PkPlayerStates.setPkPatterns(states, PlayerId.P1, PkPatterns.EMPTY);

        // Fabrique 1 contient : 1 tuile D et 1 tuile E
        int[] sources = new int[6];
        int factory1 = PkTileSet.of(1, TileKind.Colored.D);
        factory1 = PkTileSet.add(factory1, TileKind.Colored.E);
        sources[1] = factory1;

        // Simulation des sources uniques (uniquement la fabrique 1 est valide)
        int pkUniqueTileSources = PkIntSet32.add(PkIntSet32.EMPTY, 1);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), pkUniqueTileSources, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        short[] moves = new short[216];
        int moveCount = state.validMoves(moves);

        // Analyse attendue :
        // Couleur E choisie : Lignes 1, 2, 3, 4, 5 + Plancher = 6 coups
        // Couleur D choisie : Lignes 1, 2, 4, 5 + Plancher = 5 coups (LIGNE 3 INTERDITE CAR DÉJÀ DANS LE MUR)
        // Total attendu : 11 coups

        assertEquals(11, moveCount, "Les coups générés n'ont pas respecté les couleurs déjà présentes dans le mur.");
    }

    @Test
    void validMovesAlwaysAllowsFloorEvenIfFull() {
        Game game = createGame2P();
        int[] states = new int[8];

        // On remplit TOUTES les lignes de motif pour empêcher de jouer dessus
        int patterns = PkPatterns.EMPTY;
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.B);
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_3, 3, TileKind.Colored.C);
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_4, 4, TileKind.Colored.D);
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.E);
        PkPlayerStates.setPkPatterns(states, PlayerId.P1, patterns);

        // On remplit AUSSI le plancher au maximum (7 tuiles)
        int fullFloor = PkFloor.EMPTY;
        int sevenA = PkTileSet.of(7, TileKind.Colored.A);
        fullFloor = PkFloor.withAddedTiles(fullFloor, sevenA);
        PkPlayerStates.setPkFloor(states, PlayerId.P1, fullFloor);

        int[] sources = new int[6];
        // Une fabrique avec une pauvre tuile B
        sources[1] = PkTileSet.of(1, TileKind.Colored.B);
        int pkUniqueTileSources = PkIntSet32.add(PkIntSet32.EMPTY, 1);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), pkUniqueTileSources, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        short[] moves = new short[216];
        int moveCount = state.validMoves(moves);

        // Analyse attendue :
        // Toutes les lignes de motif sont pleines -> INTERDIT.
        // Le plancher est plein, mais à Azul, on peut TOUJOURS jeter sur le plancher !
        // Donc il doit y avoir exactement 1 coup valide : Mettre B sur le Plancher.

        assertEquals(1, moveCount, "Le plancher doit toujours être une destination valide, même s'il est déjà plein !");
    }

    @Test
    void validMovesDoesNotDuplicateMovesForMultipleTilesOfSameColor() {
        Game game = createGame2P(); // Toujours ton mock de jeu à 2 joueurs
        int[] states = new int[8]; // Plateaux vides

        int[] sources = new int[6];
        // Fabrique 1 contient EXACTEMENT 4 tuiles de la couleur A (et rien d'autre)
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);

        int pkUniqueTileSources = PkIntSet32.add(PkIntSet32.EMPTY, 1);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), pkUniqueTileSources, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        short[] moves = new short[216];
        int moveCount = state.validMoves(moves);

        // Analyse attendue :
        // La source 1 ne propose qu'UNE SEULE couleur jouable (A).
        // Sur un plateau vide, la couleur A peut aller sur : Lignes 1, 2, 3, 4, 5 + Plancher.
        // Total attendu = 6 coups.
        // Si ton code renvoie 24 (6 coups * 4 tuiles), c'est que tu itères sur le nombre de tuiles au lieu d'itérer sur les 5 TileKind.Colored possibles !
        assertEquals(6, moveCount, "L'algorithme a généré des coups en double pour une couleur présente en plusieurs exemplaires.");
    }

    @Test
    void validMovesCorrectlyCombinesAllBlockingConditions() {
        Game game = createGame2P();
        int[] states = new int[8];

        // Ligne 1 : Bloquée par CAPACITÉ (Pleine avec du A)
        int patterns = PkPatterns.EMPTY;
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);

        // Ligne 2 : Bloquée par COULEUR (Contient du B, on va essayer de jouer du C)
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.B);
        PkPlayerStates.setPkPatterns(states, PlayerId.P1, patterns);

        // Lignes 3, 4 et 5 : Bloquées par le MUR (Le mur possède déjà la couleur C sur ces lignes)
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_3, TileKind.Colored.C);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_4, TileKind.Colored.C);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.C);
        PkPlayerStates.setPkWall(states, PlayerId.P1, wall);

        int[] sources = new int[6];
        // Fabrique 1 ne contient QUE la couleur C
        sources[1] = PkTileSet.of(1, TileKind.Colored.C);
        int pkUniqueTileSources = PkIntSet32.add(PkIntSet32.EMPTY, 1);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), pkUniqueTileSources, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        short[] moves = new short[216];
        int moveCount = state.validMoves(moves);

        // Analyse attendue pour la couleur C :
        // Ligne 1 : Interdit (Pleine)
        // Ligne 2 : Interdit (Contient du B)
        // Lignes 3, 4, 5 : Interdit (Couleur C déjà dans le mur !)
        // Plancher : TOUJOURS AUTORISÉ.
        // Total attendu : Exactement 1 seul coup possible.
        assertEquals(1, moveCount, "Une des conditions de blocage a failli, un coup illégal a été généré sur une ligne de motif.");
    }

    @Test
    void uniqueValidMovesCompletelyIgnoresEmptyFactories() {
        Game game = createGame2P();
        int[] states = new int[8]; // Plateaux vides

        int[] sources = new int[6];
        // Centre : Seulement le marqueur
        sources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        // Fabrique 1 : Contient des tuiles
        sources[1] = PkTileSet.of(2, TileKind.Colored.A);

        // Fabrique 2, 3, 4, 5 : VIDES (0)

        // Astuce : On simule un bug où ta méthode pkUniqueTileSources aurait gardé les fabriques vides
        // en passant un masque où les bits 1, 2, 3 et 4 sont à 1.
        int badUniqueMask = (1 << 1) | (1 << 2) | (1 << 3) | (1 << 4);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), badUniqueMask, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        short[] moves = new short[216];
        int moveCount = state.uniqueValidMoves(moves);

        // Analyse attendue :
        // Même si uniqueTileSources() prétend que la source 2 est "unique", la méthode uniqueValidMoves
        // DOIT vérifier que la source n'est pas vide avant de générer des coups !
        // Seule la Fabrique 1 peut générer des coups pour la couleur A -> 6 coups (5 lignes + plancher).
        assertEquals(6, moveCount, "L'algorithme a essayé de générer des coups à partir de fabriques vides !");
    }


    @Test
    void uniqueValidMovesDoesNotTreatCenterAsAFactoryForDuplication() {
        Game game = createGame2P();
        int[] states = new int[8]; // Plateaux totalement vides pour avoir 6 coups par couleur (5 lignes + plancher)

        int[] sources = new int[6];

        // On prépare un lot de 2 tuiles A (Rouges)
        int twoA = PkTileSet.of(2, TileKind.Colored.A);

        // Le Centre (Source 0) contient 2 tuiles A
        sources[0] = twoA;

        // La Fabrique 1 (Source 1) contient AUSSI 2 tuiles A
        sources[1] = twoA;

        // La Fabrique 2 (Source 2) contient ENCORE 2 tuiles A
        sources[2] = twoA;

        // On prépare un lot de 1 tuile B (Bleue)
        int oneB = PkTileSet.of(1, TileKind.Colored.B);

        // Les Fabriques 3 et 4 contiennent 1 tuile B
        sources[3] = oneB;
        sources[4] = oneB;

        // La Fabrique 5 est vide (0)

        // --- Le calcul des sources uniques attendu ---
        // - Source 0 (Centre) : Unique (Contient A) -> bit 0
        // - Source 1 (Fab 1) : Unique ! Elle est identique au Centre, mais le Centre n'est PAS une fabrique -> bit 1
        // - Source 2 (Fab 2) : Doublon ! Elle est identique à la Fab 1 qui la précède -> ignorée
        // - Source 3 (Fab 3) : Unique (Contient B) -> bit 3
        // - Source 4 (Fab 4) : Doublon ! Identique à la Fab 3 -> ignorée
        // - Source 5 (Fab 5) : Ignorée car vide.

        int expectedUniqueMask = (1 << 0) | (1 << 1) | (1 << 3);

        ImmutableGameState state = new ImmutableGameState(
                game, 0, ImmutableIntArray.copyOf(sources), expectedUniqueMask, ImmutableIntArray.copyOf(states), PlayerId.P1
        );

        // On teste le validMoves standard pour comparer
        short[] normalMoves = new short[216];
        int normalCount = state.validMoves(normalMoves);

        // 5 sources avec du contenu * 1 couleur par source * 6 destinations possibles
        assertEquals(30, normalCount, "Le validMoves classique devrait trouver 30 coups (5 sources * 6 dest).");

        // On teste le uniqueValidMoves
        short[] uniqueMoves = new short[216];
        int uniqueCount = state.uniqueValidMoves(uniqueMoves);

        // Analyse attendue :
        // Source 0 (Unique) : Couleur A -> 6 coups
        // Source 1 (Unique) : Couleur A -> 6 coups
        // Source 3 (Unique) : Couleur B -> 6 coups
        // Total = 18 coups.

        assertEquals(18, uniqueCount,
                "L'algorithme a mal filtré ! S'il renvoie 12, c'est qu'il a considéré la Fabrique 1 " +
                        "comme un doublon du Centre (ce qui est faux selon les règles).");
    }



}