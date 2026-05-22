package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.application.Platform;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static ch.epfl.ajul.TileSource.CENTER_AREA;
import static ch.epfl.ajul.TileSource.Factory.TILES_PER_FACTORY;
import static javafx.scene.input.MouseDragEvent.MOUSE_DRAG_ENTERED;

public final class BoardUI {
    private Node root;
    private static final int WALL_PANE_OFFSET = 2;
    private static final int LINE_BONUS_OFFSET = 8;


    private Map<Pair<PlayerId, Object >, Text> bonusPointsFinderGeneral = new HashMap<>();


    private BoardUI(Node root, Map<Pair<PlayerId, Object >, Text> bonusPointsFinderGeneral){
        this.root = root;
        this.bonusPointsFinderGeneral = bonusPointsFinderGeneral;
    };

    public static BoardUI create(Map<TileLocation, Node> anchors, ObservableValue<ImmutableGameState> subject,
                          Set<Move> setMoves, boolean[] willBeHere, BlockingQueue<Move> Fifo){
        HBox allScenes = new HBox();
        Map<Pair<PlayerId, Object >, Text> bonusPointsFinder = new HashMap<>();

        GridPane tileSourcesUI = new GridPane();
        tileSourcesUI.setId("tile-sources");


        GridPane playerBoardsUI = new GridPane();
        playerBoardsUI.setId("player-boards");


        int numFactories = 0;

        for (TileSource.Factory factories : subject.getValue().game().factories()) {
            HBox factory = new HBox();
            factory.getStyleClass().addAll("tile-group", "tile-source");


            for(int j = 0; j < TILES_PER_FACTORY; j++){
                TileLocation loc = new TileLocation.OnSource(factories, j);
                Node currentNode = anchors.get(loc);
                factory.getChildren().add(currentNode);
            }

            //organiser les HBoxes dans mon GridPane
            tileSourcesUI.add(factory, numFactories % 2, numFactories / 2);
            numFactories++;
        }

        GridPane center = new GridPane();
        center.getStyleClass().addAll("tile-group", "tile-source");

        for(int i = 0; i < subject.getValue().game().centralAreaMaxSize(); i++){
            TileLocation loc = new TileLocation.OnSource(CENTER_AREA, i);
            Node currentNode = anchors.get(loc);
//            center.getChildren().add(currentNode);
            //            center.add(currentNode, i % 5,i/5); semble plus cohérent, mais ressemble pas au corrigé
            center.add(currentNode, i % 8,i/8);
        }

        tileSourcesUI.add(center, 0 , (numFactories / 2) + 1, 2 , 1);


        //todo avec les observeurs
        ObservableValue<PlayerId> currentPlayerId = subject.map(observableValue -> observableValue.currentPlayerId());

        //player scenes
        for(PlayerId Id : subject.getValue().game().playerIds()){
            StackPane playerStackPane = new StackPane();
            playerStackPane.getStyleClass().add("player-board");


            currentPlayerId.subscribe(playerId -> {
                if(Id == playerId && !playerStackPane.getStyleClass().contains("current-player")) {
                    playerStackPane.getStyleClass().add("current-player");
                }
                else{
                    playerStackPane.getStyleClass().remove("current-player");

                }
            });

            Text nameNPoints = new Text();
            nameNPoints.textProperty().bind(subject.map(gameState -> gameState.game().playerDescriptions().get(Id.ordinal()).name() +  "\n" + "Points : " + PkPlayerStates.points( gameState.pkPlayerStates(), Id ) ));


//            String.format(Id.name() +  PkPlayerStates.points( subject.getValue().pkPlayerStates(), Id ))
            VBox destinationsVBox = new VBox();

            GridPane playerGridPane = new GridPane();
            playerGridPane.getStyleClass().add("lines-and-wall");

            for(TileDestination.Pattern line : TileDestination.Pattern.ALL){

                int indexLine = line.index();

                HBox patternBox = new HBox();
                patternBox.getStyleClass().addAll("tile-destination", "tile-group");

                for(int i = 0; i < line.capacity(); i++){
                    patternBox.getChildren().add(anchors.get(new TileLocation.OnPattern(Id, line, i)));
                }


                //dans la première colonne, la ligne de motif, constituée d'une HBox contenant les ancres et ayant les classes de style tile-destination et tile-group,
                playerGridPane.add(patternBox, 0, indexLine);
                GridPane.setHalignment(patternBox, HPos.RIGHT);
                GridPane.setFillWidth(patternBox, false);

                mouseInteractive(patternBox, line, setMoves, willBeHere, Fifo, playerStackPane);


                //dans la seconde colonne, le texte de bonus de couleur (+10),
                Text colorTextBonus = BonusTextWithPlus(Points.FULL_COLOR_BONUS_POINTS);
                bonusPointsFinder.put(new Pair<>(Id, PkWall.colorAt(line, 0)), colorTextBonus);
                playerGridPane.add(colorTextBonus, 1, indexLine);



                //dans les 5 colonnes suivantes, les cases de la ligne du mur correspondant à la ligne de motif,
                // qui ont la classe de style wall-background ainsi que celle correspondant à la couleur de la
                // case (A, B, C, D ou E),
                for(int columnWall = 0; columnWall < PkWall.WALL_WIDTH ; columnWall++){
                    TileKind.Colored color = PkWall.colorAt(line, columnWall);
                    Node colorTile = anchors.get(new TileLocation.OnWall(Id, line, color));
                    colorTile.getStyleClass().addAll("wall-background", color.name() );

                    playerGridPane.add(colorTile, columnWall + WALL_PANE_OFFSET, indexLine);
                }

                //dans la huitième colonne, le texte de bonus de ligne (+2).
                Text lineBonusText = BonusTextWithPlus(Points.FULL_ROW_BONUS_POINTS);
                bonusPointsFinder.put(new Pair<>(Id, line), lineBonusText);
                playerGridPane.add(lineBonusText, LINE_BONUS_OFFSET,indexLine);

            }
            for(int columnWall = 0; columnWall < PkWall.WALL_WIDTH ; columnWall++){
                Text columnBonusPoints = BonusTextWithPlus(Points.FULL_COLUMN_BONUS_POINTS);
                bonusPointsFinder.put(new Pair<>(Id, columnWall), columnBonusPoints);
                playerGridPane.add(columnBonusPoints, columnWall + WALL_PANE_OFFSET, PkWall.WALL_HEIGHT + 1);
            }

            HBox floor = new HBox();
            floor.getStyleClass().addAll("floor", "tile-group", "tile-destination");
            //ajout du floor
            for(int i = 0; i < TileDestination.FLOOR.capacity(); i++){
                VBox tilesNPoints = new VBox();
                tilesNPoints.getChildren().add(anchors.get(new TileLocation.OnFloor(Id, i)));
                tilesNPoints.getChildren().add(BonusTextWithMinus(Points.floorPenalty(i)));
                floor.getChildren().add(tilesNPoints);
            }

            mouseInteractive(floor,  TileDestination.FLOOR, setMoves, willBeHere, Fifo, playerStackPane);



            destinationsVBox.getChildren().add(playerGridPane);
            destinationsVBox.getChildren().add(floor);
            playerStackPane.getChildren().add(nameNPoints);
            StackPane.setAlignment(nameNPoints, Pos.TOP_LEFT);
            playerStackPane.getChildren().add(destinationsVBox);


            playerBoardsUI.add(playerStackPane, Id.ordinal() % 2, Id.ordinal() / 2 );
        }





        allScenes.getChildren().addAll(tileSourcesUI, playerBoardsUI);
        return new BoardUI(allScenes, bonusPointsFinder);
    }

    private static void mouseInteractive(HBox patternBox, TileDestination line, Set<Move> setMoves, boolean[] willBeHere, BlockingQueue<Move> Fifo, StackPane playerStackPane){
        patternBox.setOnMouseDragEntered( _ -> {
            for(Move move : setMoves) {
                if (playerStackPane.getStyleClass().contains("current-player")) {
                    if (move.destination() == line) {
                        if (!patternBox.getStyleClass().contains("accepting")) {
                            patternBox.getStyleClass().add("accepting");
                        }
                        break;
                    }
                }
            }
        });

        patternBox.setOnMouseDragExited(_ ->
            patternBox.getStyleClass().remove("accepting")
        );

        patternBox.setOnMouseDragReleased(_ -> {
            if (playerStackPane.getStyleClass().contains("current-player")) {

                setMoves.stream()
                        .filter(e -> e.destination() == line)
                        .findFirst()
                        .ifPresent(move -> {
                                    willBeHere[0] = true;
                                    Fifo.offer(move);
                                    patternBox.getStyleClass().remove("accepting");
                                }
                        );
                }
        }
        );
    }

    private static Text BonusTextWithPlus(int points){
        Text text = new Text("+ " + points);
        text.setVisible(false);
        return text;
    }
    private static Text BonusTextWithMinus(int points){
        return new Text("- " + points);
    }

    public Node root(){
        return this.root;
    }

    public void showBonusPoints(PlayerId id, Object bonusKey){
        Platform.runLater(() ->
            bonusPointsFinderGeneral.get(new Pair<>(id, bonusKey)).setVisible(true));
    }


}
