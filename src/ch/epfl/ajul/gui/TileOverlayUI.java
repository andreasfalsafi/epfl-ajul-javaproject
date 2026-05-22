package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TileOverlayUI {
    private final Pane root;
    ObservableValue<ImmutableGameState> gameState;
    Tiles tiles;
    Set<Move> readOnlyValidMoves;
    Set<Move> canBePlayedWithTile;

    private static final Point2D offBoard = new Point2D(-30, -30);

    private TileOverlayUI(ObservableValue<ImmutableGameState> gameState, Tiles tiles, Set<Move> readOnlyValidMoves, Set<Move> toModifySetMove, boolean[] played, Pane pane){
    this.gameState = gameState;
    this.tiles = tiles;
    this.readOnlyValidMoves = readOnlyValidMoves;
    this.canBePlayedWithTile = toModifySetMove;
    this.root = pane;
    }

    public static TileOverlayUI create(ObservableValue<ImmutableGameState> gameState, Tiles tiles, Set<Move> readOnlyValidMoves, Set<Move> canBePlayedWithTile, boolean[] played) {

        //créer le graphe de scène
        Pane allTiles = new Pane();
        int index = 0;
        for(TileKind tileKind : TileKind.ALL) {
            List<Node> allCurrentColorNodes = tiles.tiles().get(tileKind);
            for (Node node : allCurrentColorNodes) {
                allTiles.getChildren().add(node);
                node.relocate(offBoard.getX(), offBoard.getY());
                Tiles.setLocation(node, new TileLocation.OffBoard(tileKind, index++));
            }
        }

        dragDetectHandler(tiles, readOnlyValidMoves, canBePlayedWithTile, allTiles, played, gameState);


        Function<TileLocation, Point2D> locationFunc = tileLocation -> {
            if (tileLocation instanceof TileLocation.OffBoard){
                return offBoard;
            }

            Node linkedAnchor = tiles.anchors().get(tileLocation);
            Point2D anchorLocation = linkedAnchor.localToScene(Point2D.ZERO);
            return allTiles.sceneToLocal(anchorLocation);
        };

        Platform.runLater(() -> gameState.subscribe(currentGameState ->
                TileAnimator.animateTiles(locationFunc, tiles.tiles(), currentGameState).play()
        ));

        return new TileOverlayUI(gameState, tiles, readOnlyValidMoves, canBePlayedWithTile, played, allTiles);
    }

    public Pane root(){
        return  this.root;
    }

    private static void dragDetectHandler(Tiles tiles, Set<Move> readOnlyValidMoves, Set<Move> toModifySetMove, Pane allTiles, boolean[] wasPlayed, ObservableValue<ImmutableGameState> gameState) {

        for (TileKind tileKind : TileKind.ALL) {
            List<Node> allCurrentColorNodes = tiles.tiles().get(tileKind);
            for (Node tile : allCurrentColorNodes) {
                double[] startingPoint = new double[2];
                tile.setOnMousePressed(e -> {
                    e.setDragDetect(true);
                    startingPoint[0] = e.getSceneX();
                    startingPoint[1] = e.getSceneY();
                    System.out.println("clicked");
                });

                tile.setOnDragDetected(e -> {

                    TileLocation loc = Tiles.location(tile);

                    System.out.println("--- NOUVEAU CLIC ---");
                    System.out.println("1. Emplacement logique de la tuile : " + loc);
                    if (!(loc instanceof TileLocation.OnSource)) {
                        System.out.println("🚨 BLOCAGE : La tuile n'est pas considérée comme étant sur une source !");
                    }


                        if (loc instanceof TileLocation.OnSource source) {
                        if (readOnlyValidMoves.isEmpty()) {
                            System.out.println("🚨 BLOCAGE : Il n'y a aucun coup valide dans le jeu !");                            return;
                        }
                        toModifySetMove.clear();
                        readOnlyValidMoves.stream()
                                .filter(m -> m.tileColor() == tileKind)
                                .filter(m -> m.source() == source.source())
                                .forEach(toModifySetMove::add);
                        //todo pas 100% sur
                        tile.startFullDrag();
                        allTiles.setMouseTransparent(true);

                        Set<Node> nodesToMove = new HashSet<>();
                        allCurrentColorNodes.stream()
                                .filter(m ->
                                        Tiles.location(m) instanceof TileLocation.OnSource nSource
                                                && nSource.source() == source.source())
                                .forEach(nodesToMove::add);

                        nodesToMove.forEach(p -> p.setViewOrder(-1));

                        tile.setOnMouseDragged(pointer -> {
                            for (Node n : nodesToMove) {
                                n.setTranslateX(pointer.getSceneX() - startingPoint[0]);
                                n.setTranslateY(pointer.getSceneY() - startingPoint[1]);
                            }

                        });


                        tile.setOnMouseDragDone(pointer -> {
                            for (Node node : nodesToMove) {
                                if (wasPlayed[0]) {

                                    node.setLayoutX(node.getLayoutX() + node.getTranslateX());
                                    node.setLayoutY(node.getLayoutY() + node.getTranslateY());
                                    node.setTranslateX(0);
                                    node.setTranslateY(0);

                                } else {
                                    TranslateTransition hejo = new TranslateTransition(new Duration(125), node);
                                    hejo.setToX(0);
                                    hejo.setToY(0);
                                    hejo.play();
                                }

                                node.setViewOrder(0);
                                allTiles.setMouseTransparent(false);
                            }
                            tile.setOnMouseDragDone(null);
                            tile.setOnMouseDragged(null);
                            wasPlayed[0] = false;
                        });
                    }

                });


            }

        }
    }



    public void showTilePoints(TileLocation.OnWall wall, int numPoints){
        Text pointText = new Text("+" + numPoints);

        Bounds textBound = pointText.getBoundsInLocal();
        Node anchor = tiles.anchors().get(wall);
        Point2D textPosition = this.root.sceneToLocal(anchor.localToScene(
                anchor.getBoundsInLocal().getWidth()/2,
                        anchor.getBoundsInLocal().getHeight()/2));
//                .subtract(textBound.getWidth()/2, -(textBound.getHeight()/4)));
        pointText.setX(textPosition.getX() - textBound.getCenterX());
        pointText.setY(textPosition.getY() - textBound.getCenterY());
        pointText.setViewOrder(-2);
        Platform.runLater(() -> root.getChildren().add(pointText));

    }
}