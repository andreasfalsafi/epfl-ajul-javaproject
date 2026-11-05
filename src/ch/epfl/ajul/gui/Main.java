package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Classe principale de l'application Ajul.
 *
 * <p>Cette classe orchestre le cycle de vie complet de l'application :
 * <ul>
 * <li>Analyse les arguments de ligne de commande pour configurer les joueurs (Humain/IA).</li>
 * <li>Initialise l'interface graphique JavaFX (graphe de scène).</li>
 * <li>Démarre le fil d'exécution dédié à la logique de jeu (Virtual Thread).</li>
 * <li>Gère la synchronisation entre le thread JavaFX et le thread logique via {@link javafx.application.Platform#runLater}.</li>
 * </ul>
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class Main extends Application {
    private static final int NUM_RANDOM_GEN_BYTES = 8;
    private static final int MCTS_NUM_SIM = 100_000;

    /**
     * Initialise et affiche la fenêtre principale, configure les joueurs et lance
     * le fil d'exécution gérant la partie.
     *
     * @param primaryStage la fenêtre principale de l'application JavaFX.
     * @throws Exception si une erreur survient lors de l'initialisation.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // 1. Analyse des arguments passés au programme
        Application.Parameters parameters = getParameters();
        List<String> playerNames = parameters.getUnnamed();
        int numPlayers = playerNames.size();

        if (numPlayers < 2 || numPlayers > 4) {
            throw new Error("too many players");
        }

        String seed = parameters.getNamed().get("seed");
        byte[] byteSeed = (seed != null)
                ? seed.getBytes(StandardCharsets.UTF_8)
                : SecureRandom.getSeed(NUM_RANDOM_GEN_BYTES);

        // 2. Création des joueurs
        final List<Game.PlayerDescription> playerDescriptions = getPlayerDescriptions(playerNames);

        Game currentGame = new Game(playerDescriptions);
        ImmutableGameState initial = ImmutableGameState.initial(currentGame);

        // 3. Construction de l'interface graphique
        ObjectProperty<ImmutableGameState> gameStateP = new SimpleObjectProperty<>(initial);
        boolean[] movePlayed = new boolean[1];

        Set<Move> potentialMoves = new HashSet<>();
        SynchronousQueue<Move> javaFXMoves = new SynchronousQueue<>();
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<>());

        Tiles tiles = Tiles.create(currentGame);
        BoardUI board = BoardUI.create(tiles.anchors(), gameStateP, potentialMoves, movePlayed, javaFXMoves);
        TileOverlayUI overlay = TileOverlayUI.create(gameStateP, tiles, validMoves, potentialMoves, movePlayed);

        StackPane graphicalUI = new StackPane(board.root(), overlay.root());
        graphicalUI.getStylesheets().add("ajul.css");

        primaryStage.setScene(new Scene(graphicalUI));
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);

        PointsObserver gamePointsObserver = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color, int points) {
            overlay.showTilePoints(new TileLocation.OnWall(playerId, line, color), points);
            }

            @Override
            public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
                board.showBonusPoints(playerId, color);
            }

            @Override
            public void fullColumn(PlayerId playerId, int column, int points) {
                board.showBonusPoints(playerId, column);
            }

            @Override
            public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
                board.showBonusPoints(playerId, line);            }
        };

        // 4. Lancement du fil d'exécution gérant la partie
        Thread.startVirtualThread(() -> {
            MutableGameState gameState = new MutableGameState(initial, gamePointsObserver);
            short[] validMovesArray = new short[Move.MAX_MOVES];

            MctsPlayer bot = new MctsPlayer(RandomGeneratorFactory.getDefault(), MCTS_NUM_SIM);
            RandomGenerator rng = RandomGeneratorFactory.getDefault().create(byteSeed);

            gameState.fillFactories(rng);
            //todo he said Updateview
            updateView(gameState, gameStateP);

            while (!gameState.isGameOver()){

                if (gameState.isRoundOver()) {
                    sleepFor(1000);
                    gameState.endRound();
                    updateView(gameState, gameStateP);

                    sleepFor(700);

                    if (!gameState.isGameOver()) {
                        gameState.fillFactories(rng);
                        updateView(gameState, gameStateP);
                    }

                }

                if (gameState.isGameOver()) {
                    break;
                }

                short nextMove;
                if (playerDescriptions.get(gameState.currentPlayerId().ordinal()).kind() == Game.PlayerDescription.PlayerKind.HUMAN){

                    int numValidMoves = gameState.validMoves(validMovesArray);
                    for (int i = 0; i < numValidMoves; i++) {
                        validMoves.add(Move.ofPacked(validMovesArray[i]));
                    }

                    try {
                        nextMove = javaFXMoves.take().packed();
                        validMoves.clear();
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }
                }
                else {
                    nextMove = bot.nextMove(gameState);
                }
                gameState.registerMove(nextMove);
                updateView(gameState, gameStateP);
            }

            gameState.endGame();
            updateView(gameState, gameStateP);
        });

        primaryStage.show();
    }

    private static List<Game.PlayerDescription> getPlayerDescriptions(List<String> playerNames) {
        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        int playerId = 0;

        for(String players : playerNames){
            String playerName = players;
            Game.PlayerDescription.PlayerKind kind = Game.PlayerDescription.PlayerKind.HUMAN;
            if(players.startsWith("_")){
                kind = Game.PlayerDescription.PlayerKind.AI;
                playerName= playerName.substring(1);
            }
            playerDescriptions.add(new Game.PlayerDescription(PlayerId.ALL.get(playerId++), playerName, kind ));
        }
        return playerDescriptions;
    }

    /**
     * Met à jour l'interface graphique de manière sécurisée depuis le fil d'exécution de la partie.
     */
    private static void updateView(MutableGameState state, ObjectProperty<ImmutableGameState> stateProperty) {
        ImmutableGameState immutableState = state.immutable();
        Platform.runLater(() -> stateProperty.set(immutableState));
    }

    /**
     * Suspend le fil d'exécution de la partie pour la durée spécifiée.
     */
    private static void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
    /**
     * Point d'entrée de l'application Java.
     *
     * @param args arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
