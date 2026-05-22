package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static java.lang.Thread.sleep;

public final class Main extends Application {
    private static final int NUM_RANDOM_GEN_BYTES = 8;
    private static final int MCTS_NUM_SIM = 100_000;

    @Override
    public void start(Stage primaryStage) throws Exception {

        //todo CLAIREMENT UN PROBLèME AVEC LES PATTERNS
        //analyser les arguments passés au programme afin de configurer la partie
        Application.Parameters parameters = getParameters();

        //puisque les joueurs sont les arguments non-nommées, la taille de la lsiter des arguments non-nommé est égal au nombre de joueur
        List<String> playerNames = parameters.getUnnamed();

        //démarrer le fil d'éxécution gérant la partie
        SynchronousQueue<Move> javaFXMoves = new SynchronousQueue<>();
        //set de moves communiquable entre les deux threads
        Set<Move> validMoves =
                Collections.synchronizedSet(new HashSet<>());

        int numPlayers = playerNames.size();

        if (numPlayers < 2 || numPlayers > 4) {
            throw new Error("too many players");
        }
        String seed = parameters.getNamed().get("seed");

        byte[] byteSeed;
        //seeded aléatoire
        if (!(seed == null)) {
            byteSeed = seed.getBytes();
        }
        //vraiment aléatoire
        else {
            byteSeed = SecureRandom.getSeed(NUM_RANDOM_GEN_BYTES);
        }



        //créer Game
        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        int playerId = 0;
        for(String players : playerNames){
            String playerNameTemp = players;
            Game.PlayerDescription.PlayerKind playerKind = Game.PlayerDescription.PlayerKind.HUMAN;
            if(players.charAt(0) == '_'){
                playerKind = Game.PlayerDescription.PlayerKind.AI;
                //starts at one (1) to remove the first "_"
                playerNameTemp = playerNameTemp.substring(1);
            }
            playerDescriptions.add(new Game.PlayerDescription(PlayerId.ALL.get(playerId++), playerNameTemp, playerKind ));
        }


        Game currentGame = new Game(playerDescriptions);

        ImmutableGameState initial = ImmutableGameState.initial(currentGame);


        //construire l'interface graphique principale

        ObjectProperty<ImmutableGameState> gameStateP = new SimpleObjectProperty<>(initial);
        boolean[] movePlayed = new boolean[1];
        Tiles tiles = Tiles.create(currentGame);
        Set<Move> potentialMoves = new HashSet<>();
        BoardUI board = BoardUI.create(tiles.anchors(), gameStateP, potentialMoves, movePlayed, javaFXMoves);
        TileOverlayUI overlay = TileOverlayUI.create(gameStateP, tiles, validMoves, potentialMoves, movePlayed);
        StackPane graphicalUI = new StackPane();

        //TODO not sure on getSyleSheets, is it getstyleclass or setId or smth?
        graphicalUI.getStylesheets().add("ajul.css");
        graphicalUI.getChildren().add(board.root());
        graphicalUI.getChildren().add(overlay.root());

        primaryStage.setScene(new Scene(graphicalUI));

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
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);

        //toute la gérance de game
        Thread.startVirtualThread(() -> {
//            ImmutableGameState initial = ImmutableGameState.initial(currentGame);


            MutableGameState gameState = new MutableGameState(initial, gamePointsObserver);
            short[] validMovesArray = new short[Move.MAX_MOVES];
//            ImmutableGameState immutableGameState = gameState.immutable();
//            Platform.runLater(() -> gameStateP.set(immutableGameState));

            MctsPlayer bot = new MctsPlayer(RandomGeneratorFactory.getDefault(), MCTS_NUM_SIM);
            RandomGenerator rng = RandomGeneratorFactory.getDefault().create(byteSeed);

            gameState.fillFactories(rng);
            Platform.runLater(() -> gameStateP.set(gameState.immutable()));

            while (!gameState.isGameOver()){

                if (gameState.isRoundOver()) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }
                    gameState.endRound();
                    Platform.runLater(() -> gameStateP.set(gameState.immutable()));
                    try {
                        sleep(700);
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }
                    gameState.fillFactories(rng);
                    Platform.runLater(() -> gameStateP.set(gameState.immutable()));

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
                Platform.runLater(() -> gameStateP.set(gameState.immutable()));
            }

            //TODO ya plus là
            gameState.endGame();
            Platform.runLater(() -> gameStateP.set(gameState.immutable()));



        });

        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
