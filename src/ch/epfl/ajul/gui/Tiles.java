package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.epfl.ajul.TileSource.CENTER_AREA;
import static ch.epfl.ajul.TileSource.Factory.TILES_PER_FACTORY;

public record Tiles(Map<TileLocation, Node> anchors, Map<TileKind, List<Node>> tiles) {
    public static final int TILE_WIDTH = 30;
    public static final int TILE_HEIGHT = 30;

    public static Tiles create(Game game){


        Stream<TileLocation> factoryLocations = game.factories().stream()
                .flatMap(factory -> IntStream.range(0, TILES_PER_FACTORY)
                        .mapToObj(index -> new TileLocation.OnSource(factory, index)));

        Stream<TileLocation> centerLocation = game.tileSources().stream()
                .filter(tileSource -> tileSource == CENTER_AREA)
                .flatMap(centerArea -> IntStream.range(0, game.centralAreaMaxSize())
                        .mapToObj(index -> new TileLocation.OnSource(centerArea, index)));

        Stream<TileLocation> patternLocations = game.playerIds().stream()
                .flatMap(playerId -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, line.capacity())
                                .mapToObj(index -> new TileLocation.OnPattern(playerId, line, index))));

        Stream<TileLocation> floorsLocations = game.playerIds().stream()
                .flatMap(playerId -> IntStream.range(0,TileDestination.Floor.FLOOR.capacity())
                        .mapToObj(i -> new TileLocation.OnFloor(playerId, i)));

        Stream<TileLocation> wallLocations = game.playerIds().stream()
                .flatMap(playerId -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, PkWall.WALL_WIDTH)
                                .mapToObj(index -> new TileLocation.OnWall(playerId, line, PkWall.colorAt(line, index)))));

        List<TileLocation> allLocations = Stream.of(
                factoryLocations, centerLocation, patternLocations, floorsLocations, wallLocations
        ).flatMap(stream -> stream).toList();

        Map<TileLocation, Node> anchors = allLocations.stream()
                .collect(Collectors.toMap(
                        location -> location,
                        location -> {

//                            Node thisAnchor = anchor();
//                            setLocation(thisAnchor, location);
                            return anchor();
                        }));

        Map<TileKind, List<Node>> tiles = TileKind.ALL.stream().collect(Collectors
                .toMap(color -> color, color -> {


                    List<Node> all = new ArrayList<>();
                    if(color != TileKind.FIRST_PLAYER_MARKER) {
                        for (int i = 0; i < color.tilesCount(); i++) {
                            Node thisColorTiles = tile();
                            thisColorTiles.getStyleClass().add(color.toString());
                            all.add(thisColorTiles);
                        }
                    }
                    else{
                        all.add(FPM());
                    }

                    return all;
                }));

        return new Tiles(Map.copyOf(anchors), Map.copyOf(tiles) );
    }

    private static Node FPM(){
        Text text = new Text("1");
        StackPane FPM = new StackPane(tile(), text);
        FPM.setId("first-player-marker");
        return  FPM;
    }
    private static Node tile(){
        Node tile = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
        tile.getStyleClass().add("tile");
        return tile;
    }
    private static Node anchor(){
        Node anchor = tile();
        anchor.getStyleClass().add( "anchor");
        return anchor;
    }
    public static TileLocation location(Node node){
        return (TileLocation) node.getUserData();
    }

    public static void setLocation(Node node, TileLocation location){
        node.setUserData(location);
    }
}
