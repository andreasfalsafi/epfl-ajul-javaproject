package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileKind;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Gère la représentation graphique et l'interaction des tuiles au-dessus du plateau de jeu.
 * <p>
 * Cette classe s'occupe de l'affichage des tuiles (via le positionnement au-dessus de leurs ancres),
 * de la gestion du glisser-déposer par l'utilisateur humain, ainsi que de l'affichage ponctuel
 * des points rapportés par les tuiles placées sur le mur en fin de manche.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class TileOverlayUI {
    private final Pane root;
    private final Tiles tiles;

    private static final Point2D OFF_BOARD_POS = new Point2D(-30, -30);

    /**
     * Constructeur privé pour instancier l'interface des tuiles.
     * L'instanciation doit se faire via la méthode fabrique {@link #create}.
     */
    private TileOverlayUI(Tiles tiles, Pane pane) {
        this.tiles = tiles;
        this.root = pane;
    }

    /**
     * Crée et retourne une nouvelle instance de l'interface gérant les tuiles.
     * Cette méthode initialise le panneau, place les tuiles hors du plateau et configure
     * l'observation de l'état de jeu pour déclencher les animations.
     *
     * @param gameState           la valeur observable de l'état immuable de la partie
     * @param tiles               l'enregistrement contenant les nœuds des tuiles et des ancres
     * @param readOnlyValidMoves  l'ensemble des coups valides pour l'état courant de la partie
     * @param canBePlayedWithTile l'ensemble des coups possibles avec la tuile actuellement survolée/déplacée
     * @param moveAccepted        un tableau booléen utilisé pour valider la réussite d'un dépôt par l'utilisateur
     * @return une instance entièrement configurée de TileOverlayUI
     */
    public static TileOverlayUI create(ObservableValue<ImmutableGameState> gameState, Tiles tiles, Set<Move> readOnlyValidMoves, Set<Move> canBePlayedWithTile, boolean[] moveAccepted) {

        // Créer le graphe de scène
        Pane allTiles = new Pane();
        int index = 0;
        for (TileKind tileKind : TileKind.ALL) {
            List<Node> allCurrentColorNodes = tiles.tiles().get(tileKind);
            for (Node node : allCurrentColorNodes) {
                allTiles.getChildren().add(node);
                node.relocate(OFF_BOARD_POS.getX(), OFF_BOARD_POS.getY());
                Tiles.setLocation(node, new TileLocation.OffBoard(tileKind, index++));

                setUpTileInteraction(node, tileKind, allCurrentColorNodes, readOnlyValidMoves, canBePlayedWithTile, allTiles, moveAccepted);

            }
        }


        Function<TileLocation, Point2D> locationFunc = tileLocation -> {
            if (tileLocation instanceof TileLocation.OffBoard) return OFF_BOARD_POS;

            Node linkedAnchor = tiles.anchors().get(tileLocation);
            Point2D anchorLocation = linkedAnchor.localToScene(Point2D.ZERO);
            return allTiles.sceneToLocal(anchorLocation);
        };

        Platform.runLater(() -> gameState.subscribe(currentGameState ->
                TileAnimator.animateTiles(locationFunc, tiles.tiles(), currentGameState).play()
        ));

        return new TileOverlayUI(tiles, allTiles);
    }

    /**
     * Retourne la racine du graphe de scène géré par cette classe.
     *
     * @return le panneau (Pane) contenant l'ensemble des tuiles et des textes de points
     */
    public Pane root() {
        return this.root;
    }

    /**
     * Configure les événements de glisser-déposer (Drag & Drop) pour une tuile spécifique.
     * <p>
     * Cette méthode attache les gestionnaires nécessaires pour détecter le début du glissement,
     * filtrer les coups valides, déplacer visuellement les tuiles de la même couleur provenant
     * de la même source, et valider ou annuler le déplacement lorsque la souris est relâchée.
     *
     * @param tile            le nœud JavaFX représentant la tuile à configurer
     * @param tileKind        la couleur (sorte) de cette tuile
     * @param allNodesOfColor la liste de tous les nœuds représentant des tuiles de cette même couleur
     * @param validMoves      l'ensemble des coups valides pour l'état courant de la partie
     * @param currentMoves    l'ensemble destiné à stocker temporairement les coups possibles avec cette tuile
     * @param allTiles        le panneau contenant toutes les tuiles (utilisé pour modifier la transparence à la souris)
     * @param moveAccepted    un tableau booléen à un élément servant de drapeau pour valider la réussite du dépôt
     */
    private static void setUpTileInteraction(Node tile, TileKind tileKind, List<Node> allNodesOfColor, Set<Move> validMoves, Set<Move> currentMoves, Pane allTiles, boolean[] moveAccepted) {

        final double[] startingPoint = new double[2];

        tile.setOnMousePressed(e -> {
            e.setDragDetect(true);
            startingPoint[0] = e.getSceneX();
            startingPoint[1] = e.getSceneY();
        });

        tile.setOnDragDetected(e -> {

            TileLocation loc = Tiles.location(tile);

            if (!(loc instanceof TileLocation.OnSource source)) return;
            if (validMoves.isEmpty()) return;

            currentMoves.clear();
            validMoves.stream()
                    .filter(m -> m.tileColor() == tileKind)
                    .filter(m -> m.source() == source.source())
                    .forEach(currentMoves::add);

            tile.startFullDrag();
            allTiles.setMouseTransparent(true);

            Set<Node> nodesToMove = new HashSet<>();
            allNodesOfColor.stream()
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
                    if (moveAccepted[0]) {
                        node.setLayoutX(node.getLayoutX() + node.getTranslateX());
                        node.setLayoutY(node.getLayoutY() + node.getTranslateY());
                        node.setTranslateX(0);
                        node.setTranslateY(0);
                    } else {
                        TranslateTransition transition = new TranslateTransition(Duration.millis(125), node);
                        transition.setToX(0);
                        transition.setToY(0);
                        transition.play();
                    }

                    node.setViewOrder(0);
                }

                allTiles.setMouseTransparent(false);
                tile.setOnMouseDragDone(null);
                tile.setOnMouseDragged(null);
                moveAccepted[0] = false;
            });
        });
    }

    /**
     * Affiche une animation textuelle des points gagnés au-dessus d'une tuile placée sur le mur.
     * Le texte est généré, centré précisément sur l'ancre de destination, et placé dans la couche appropriée.
     * L'ajout au graphe de scène est exécuté de manière sécurisée dans le fil d'exécution de JavaFX.
     *
     * @param wall      l'emplacement de la tuile sur le mur
     * @param numPoints le nombre de points rapportés par cette tuile
     */
    public void showTilePoints(TileLocation.OnWall wall, int numPoints) {
        Text pointText = new Text("+" + numPoints);

        Bounds textBound = pointText.getBoundsInLocal();
        Node anchor = tiles.anchors().get(wall);
        Point2D textPosition = this.root.sceneToLocal(anchor.localToScene(
                anchor.getBoundsInLocal().getWidth() / 2,
                anchor.getBoundsInLocal().getHeight() / 2));

        pointText.setX(textPosition.getX() - textBound.getCenterX());
        pointText.setY(textPosition.getY() - textBound.getCenterY());
        pointText.setViewOrder(-2);
        Platform.runLater(() -> root.getChildren().add(pointText));

    }
}