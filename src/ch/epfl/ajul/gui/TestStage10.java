package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

public final class TestStage10 extends Application {
    @Override
    public void start(Stage primaryStage) {
        // … comme `TestBoardUI`, mais `gameStateP` a le type
        //   `ObjectProperty<…>`, pas `ObservableValue<…>`.
        int playersCount = 4;
        Game game = new Game(PlayerId.ALL.stream()
                .map(pId ->
                        new Game.PlayerDescription(pId, pId.name(), HUMAN))
                .limit(playersCount)
                .toList());
        ImmutableGameState initialGameState =
                ImmutableGameState.initial(game);

        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        BlockingQueue<Move> moveQueue = new SynchronousQueue<>();

        Tiles tiles = Tiles.create(game);
        BoardUI boardUI = BoardUI.create(tiles.anchors(),
                gameStateP,
                potentialMoves,
                moveAccepted,
                moveQueue);
//        boardUI.showBonusPoints(PlayerId.P2, TileDestination.Pattern.PATTERN_3);




        Set<Move> validMoves = new HashSet<>();
        TileOverlayUI tileOverlayUI =
                TileOverlayUI.create(gameStateP,
                        tiles,
                        validMoves,
                        potentialMoves,
                        moveAccepted);

        Parent root = new StackPane(boardUI.root(),
                tileOverlayUI.root());
        Platform.runLater(() -> {
            MutableGameState gameState =
                    new MutableGameState(initialGameState);
            gameState.fillFactories(RandomGeneratorFactory
                    .getDefault()
                    .create(2026));
            ImmutableGameState immutableGameState =
                    gameState.immutable();
            // TODO remplir `validMoves` avec les coups valides.
            short[] allValidPkMoves = new short[216];
            int howMuch = immutableGameState.validMoves(allValidPkMoves);
            for(int i = 0; i < howMuch; i++){
                validMoves.add(Move.ofPacked(allValidPkMoves[i]));
            }

            gameStateP.set(immutableGameState);
        });

        root.getStylesheets().add("ajul.css");
        // … comme `TestBoardUI`
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(TestBoardUI.class.getSimpleName());
        primaryStage.show();
    }
}