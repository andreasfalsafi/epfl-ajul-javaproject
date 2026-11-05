package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static ch.epfl.ajul.TileSource.CENTER_AREA;
import static ch.epfl.ajul.TileSource.Factory.TILES_PER_FACTORY;


/**
 * Gère l'interface graphique du plateau de jeu Ajul.
 * Cette classe construit le graphe de scène complet incluant les sources de tuiles
 * (fabriques et zone centrale) et les plateaux individuels des joueurs.
 * * <p>Elle assure la mise à jour dynamique de l'affichage via des observateurs
 * (scores, joueur courant) et gère l'interaction de glisser-déposer des tuiles.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class BoardUI {
    private final Node root;
    private final Map<Pair<PlayerId, Object>, Text> bonusPointsFinderGeneral;

    private static final int WALL_PANE_OFFSET = 2;
    private static final int LINE_BONUS_OFFSET = 8;

    /**
     * Constructeur privé. La création doit passer par {@link #create}.
     */
    private BoardUI(Node root, Map<Pair<PlayerId, Object>, Text> bonusPointsFinderGeneral) {
        this.root = root;
        this.bonusPointsFinderGeneral = bonusPointsFinderGeneral;
    }

    ;

    /**
     * Construit et initialise l'interface graphique du plateau de jeu.
     *
     * @param anchors      table associative liant les ancres aux nœuds graphiques.
     * @param subject      valeur observable de l'état immuable de la partie.
     * @param validMoves   ensemble des coups valides pour l'interaction.
     * @param moveAccepted tableau booléen de contrôle pour le dépôt réussi (drag & drop).
     * @param moveQueue    file bloquante pour transmettre le coup choisi au moteur de jeu.
     * @return une instance configurée de {@code BoardUI}.
     */
    public static BoardUI create(Map<TileLocation, Node> anchors, ObservableValue<ImmutableGameState> subject,
                                 Set<Move> validMoves, boolean[] moveAccepted, BlockingQueue<Move> moveQueue) {

        HBox allScenes = new HBox();
        Map<Pair<PlayerId, Object>, Text> bonusPointsFinder = new HashMap<>();

        // 1. Construction de la zone des sources (gauche)
        GridPane tileSourcesUI = buildTileSources(subject.getValue().game(), anchors);

        // 2. Construction de la zone des joueurs (droite)
        GridPane playerBoardsUI = new GridPane();
        playerBoardsUI.setId("player-boards");

        for (PlayerId id : subject.getValue().game().playerIds()) {
            StackPane playerBoard = buildPlayerBoard(id, subject, anchors, validMoves, moveAccepted, moveQueue, bonusPointsFinder);
            playerBoardsUI.add(playerBoard, id.ordinal() % 2, id.ordinal() / 2);
        }

        allScenes.getChildren().addAll(tileSourcesUI, playerBoardsUI);

        return new BoardUI(allScenes, bonusPointsFinder);

    }

    private static void setUpInteractiveDestination(HBox patternBox, TileDestination line,
                                                    Set<Move> validMoves, boolean[] willBeHere,
                                                    BlockingQueue<Move> moveQueue, StackPane playerStackPane) {

        patternBox.setOnMouseDragEntered(_ -> {
            if (playerStackPane.getStyleClass().contains("current-player")) {
                boolean isValidForDestination = validMoves.stream().anyMatch(move -> move.destination() == line);
                if (isValidForDestination && !patternBox.getStyleClass().contains("accepting")) {
                    patternBox.getStyleClass().add("accepting");
                }
            }
        });

        patternBox.setOnMouseDragExited(_ -> patternBox.getStyleClass().remove("accepting"));

        patternBox.setOnMouseDragReleased(_ -> {
                    if (playerStackPane.getStyleClass().contains("current-player")) {

                        validMoves.stream()
                                .filter(e -> e.destination() == line)
                                .findFirst()
                                .ifPresent(move -> {
                                            willBeHere[0] = true;
                                            moveQueue.offer(move);
                                            patternBox.getStyleClass().remove("accepting");
                                        }
                                );
                    }
                }
        );
    }

    private static GridPane buildTileSources(Game game, Map<TileLocation, Node> anchors) {
        GridPane tileSourcesUI = new GridPane();
        tileSourcesUI.setId("tile-sources");

        int numFactories = 0;

        for (TileSource.Factory factories : game.factories()) {
            HBox factory = new HBox();
            factory.getStyleClass().addAll("tile-group", "tile-source");


            for (int j = 0; j < TILES_PER_FACTORY; j++) {
                TileLocation loc = new TileLocation.OnSource(factories, j);
                Node currentNode = anchors.get(loc);
                factory.getChildren().add(currentNode);
            }

            tileSourcesUI.add(factory, numFactories % 2, numFactories / 2);
            numFactories++;
        }

        GridPane center = new GridPane();
        center.getStyleClass().addAll("tile-group", "tile-source");

        for (int i = 0; i < game.centralAreaMaxSize(); i++) {
            TileLocation loc = new TileLocation.OnSource(CENTER_AREA, i);
            Node currentNode = anchors.get(loc);

            center.add(currentNode, i % 8, i / 8);
        }

        tileSourcesUI.add(center, 0, (numFactories / 2) + 1, 2, 1);

        return tileSourcesUI;
    }

    private static StackPane buildPlayerBoard(PlayerId id, ObservableValue<ImmutableGameState> subject,
                                              Map<TileLocation, Node> anchors, Set<Move> validMoves,
                                              boolean[] moveAccepted, BlockingQueue<Move> moveQueue,
                                              Map<Pair<PlayerId, Object>, Text> bonusMap) {

        StackPane playerStackPane = new StackPane();
        playerStackPane.getStyleClass().add("player-board");

        ObservableValue<PlayerId> currentPlayerId = subject.map(ImmutableGameState::currentPlayerId);

        currentPlayerId.subscribe(playerId -> {
            if (id == playerId && !playerStackPane.getStyleClass().contains("current-player")) {
                playerStackPane.getStyleClass().add("current-player");
            } else {
                playerStackPane.getStyleClass().remove("current-player");

            }
        });

        String playerName = subject.getValue().game().playerDescriptions().get(id.ordinal()).name();
        Text nameNPoints = new Text();

        nameNPoints.textProperty().bind(Bindings.format("%s\nPoints : %d",
                playerName,
                subject.map(gameState -> PkPlayerStates.points(gameState.pkPlayerStates(), id))
        ));
        StackPane.setAlignment(nameNPoints, Pos.TOP_LEFT);


        VBox destinationsVBox = new VBox();
        GridPane playerGridPane = new GridPane();
        playerGridPane.getStyleClass().add("lines-and-wall");

        // Construction des lignes de motifs et murs
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {

            int indexLine = line.index();

            HBox patternBox = new HBox();
            patternBox.getStyleClass().addAll("tile-destination", "tile-group");

            for (int i = 0; i < line.capacity(); i++) {
                patternBox.getChildren().add(anchors.get(new TileLocation.OnPattern(id, line, i)));
            }

            playerGridPane.add(patternBox, 0, indexLine);
            GridPane.setHalignment(patternBox, HPos.RIGHT);
            GridPane.setFillWidth(patternBox, false);

            setUpInteractiveDestination(patternBox, line, validMoves, moveAccepted, moveQueue, playerStackPane);

            // Bonus couleur
            Text colorTextBonus = BonusTextWithPlus(Points.FULL_COLOR_BONUS_POINTS);
            bonusMap.put(new Pair<>(id, PkWall.colorAt(line, 0)), colorTextBonus);
            playerGridPane.add(colorTextBonus, 1, indexLine);


            // Mur
            for (int columnWall = 0; columnWall < PkWall.WALL_WIDTH; columnWall++) {
                TileKind.Colored color = PkWall.colorAt(line, columnWall);
                Node colorTile = anchors.get(new TileLocation.OnWall(id, line, color));
                colorTile.getStyleClass().addAll("wall-background", color.name());
                playerGridPane.add(colorTile, columnWall + WALL_PANE_OFFSET, indexLine);
            }

            // Bonus ligne
            Text lineBonusText = BonusTextWithPlus(Points.FULL_ROW_BONUS_POINTS);
            bonusMap.put(new Pair<>(id, line), lineBonusText);
            playerGridPane.add(lineBonusText, LINE_BONUS_OFFSET, indexLine);
        }

        // Bonus colonne
        for (int columnWall = 0; columnWall < PkWall.WALL_WIDTH; columnWall++) {
            Text columnBonusPoints = BonusTextWithPlus(Points.FULL_COLUMN_BONUS_POINTS);
            bonusMap.put(new Pair<>(id, columnWall), columnBonusPoints);
            playerGridPane.add(columnBonusPoints, columnWall + WALL_PANE_OFFSET, PkWall.WALL_HEIGHT + 1);
        }

        // Construction du plancher
        HBox floor = new HBox();
        floor.getStyleClass().addAll("floor", "tile-group", "tile-destination");

        for (int i = 0; i < TileDestination.FLOOR.capacity(); i++) {
            VBox tilesNPoints = new VBox();
            tilesNPoints.getChildren().add(anchors.get(new TileLocation.OnFloor(id, i)));
            tilesNPoints.getChildren().add(BonusTextWithMinus(Points.floorPenalty(i)));
            floor.getChildren().add(tilesNPoints);
        }

        setUpInteractiveDestination(floor, TileDestination.FLOOR, validMoves, moveAccepted, moveQueue, playerStackPane);

        destinationsVBox.getChildren().addAll(playerGridPane, floor);
        playerStackPane.getChildren().addAll(nameNPoints, destinationsVBox);

        return playerStackPane;
    }

    private static Text BonusTextWithPlus(int points) {
        Text text = new Text("+ " + points);
        text.setVisible(false);
        return text;
    }

    private static Text BonusTextWithMinus(int points) {
        return new Text("- " + points);
    }

    /**
     * Retourne le nœud racine du graphe de scène représentant le plateau.
     *
     * @return le nœud racine de l'interface du plateau.
     */
    public Node root() {
        return this.root;
    }

    /**
     * Affiche les points bonus sur le plateau du joueur spécifié.
     *
     * @param id       l'identité du joueur concerné.
     * @param bonusKey la clé identifiant le bonus ({@link TileDestination.Pattern},
     *                 {@link Integer} pour les colonnes, ou {@link TileKind.Colored} pour les couleurs).
     */
    public void showBonusPoints(PlayerId id, Object bonusKey) {
        Platform.runLater(() ->
                bonusPointsFinderGeneral.get(new Pair<>(id, bonusKey)).setVisible(true));
    }


}
