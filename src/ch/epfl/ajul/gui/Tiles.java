package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
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

/**
 * Représente la collection complète des ancres et des tuiles du jeu.
 *
 * <p>Cette classe gère la création des nœuds JavaFX correspondants aux emplacements
 * du plateau (ancres) et aux tuiles physiques. Elle assure également la liaison
 * entre ces objets graphiques et leurs emplacements logiques via les métadonnées.
 *
 * @param anchors table associant chaque {@link TileLocation} à son nœud d'ancrage.
 * @param tiles   table associant chaque {@link TileKind} à la liste de ses tuiles graphiques.
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public record Tiles(Map<TileLocation, Node> anchors, Map<TileKind, List<Node>> tiles) {

    /**
     * Largeur standard d'une tuile en unités JavaFX.
     */
    public static final int TILE_WIDTH = 30;

    /**
     * Hauteur standard d'une tuile en unités JavaFX.
     */
    public static final int TILE_HEIGHT = 30;

    /**
     * Crée une instance de {@code Tiles} contenant tous les éléments graphiques
     * nécessaires pour une partie donnée.
     *
     * @param game la configuration de la partie
     * @return une nouvelle instance de {@code Tiles}
     */
    public static Tiles create(Game game) {


        Stream<TileLocation> factoryLocations = getTileLocationStream(game);

        Stream<TileLocation> centerLocation = getCenterLocationStream(game);

        Stream<TileLocation> patternLocations = getPatternLocationStream(game);

        Stream<TileLocation> floorsLocations = getFloorsLocationStream(game);

        Stream<TileLocation> wallLocations = getWallLocationsStream(game);

        List<TileLocation> allLocations = Stream.of(
                factoryLocations, centerLocation, patternLocations, floorsLocations, wallLocations
        ).flatMap(stream -> stream).toList();

        Map<TileLocation, Node> anchors = allLocations.stream()
                .collect(Collectors.toMap(
                        location -> location,
                        _ -> anchor()));

        Map<TileKind, List<Node>> tiles = createAllTiles();

        return new Tiles(Map.copyOf(anchors), Map.copyOf(tiles));
    }

    /**
     * Retourne l'emplacement logique associé au nœud donné.
     *
     * @param node le nœud JavaFX (tuile ou ancre)
     * @return son emplacement {@link TileLocation}
     */
    public static TileLocation location(Node node) {
        return (TileLocation) node.getUserData();
    }

    /**
     * Associe un emplacement logique au nœud donné.
     *
     * @param node     le nœud JavaFX (tuile ou ancre)
     * @param location l'emplacement {@link TileLocation} à associer
     */
    public static void setLocation(Node node, TileLocation location) {
        node.setUserData(location);
    }

    private static Map<TileKind, List<Node>> createAllTiles() {
        Map<TileKind, List<Node>> tiles = TileKind.ALL.stream().collect(Collectors
                .toMap(color -> color, color -> {


                    List<Node> all = new ArrayList<>();
                    if (color != TileKind.FIRST_PLAYER_MARKER) {
                        for (int i = 0; i < color.tilesCount(); i++) {
                            Node thisColorTiles = tile();
                            thisColorTiles.getStyleClass().add(color.toString());
                            all.add(thisColorTiles);
                        }
                    } else {
                        all.add(FPM());
                    }

                    return all;
                }));
        return tiles;
    }

    private static Stream<TileLocation> getWallLocationsStream(Game game) {
        Stream<TileLocation> wallLocations = game.playerIds().stream()
                .flatMap(playerId -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, PkWall.WALL_WIDTH)
                                .mapToObj(index -> new TileLocation.OnWall(playerId, line, PkWall.colorAt(line, index)))));
        return wallLocations;
    }

    private static Stream<TileLocation> getFloorsLocationStream(Game game) {
        Stream<TileLocation> floorsLocations = game.playerIds().stream()
                .flatMap(playerId -> IntStream.range(0, TileDestination.Floor.FLOOR.capacity())
                        .mapToObj(i -> new TileLocation.OnFloor(playerId, i)));
        return floorsLocations;
    }

    private static Stream<TileLocation> getPatternLocationStream(Game game) {
        Stream<TileLocation> patternLocations = game.playerIds().stream()
                .flatMap(playerId -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, line.capacity())
                                .mapToObj(index -> new TileLocation.OnPattern(playerId, line, index))));
        return patternLocations;
    }

    private static Stream<TileLocation> getCenterLocationStream(Game game) {
        Stream<TileLocation> centerLocation = game.tileSources().stream()
                .filter(tileSource -> tileSource == CENTER_AREA)
                .flatMap(centerArea -> IntStream.range(0, game.centralAreaMaxSize())
                        .mapToObj(index -> new TileLocation.OnSource(centerArea, index)));
        return centerLocation;
    }

    private static Stream<TileLocation> getTileLocationStream(Game game) {
        Stream<TileLocation> factoryLocations = game.factories().stream()
                .flatMap(factory -> IntStream.range(0, TILES_PER_FACTORY)
                        .mapToObj(index -> new TileLocation.OnSource(factory, index)));
        return factoryLocations;
    }

    private static Node FPM() {
        Text text = new Text("1");
        StackPane FPM = new StackPane(tile(), text);
        FPM.setId("first-player-marker");
        return FPM;
    }

    private static Node tile() {
        Node tile = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
        tile.getStyleClass().add("tile");
        return tile;
    }

    private static Node anchor() {
        Node anchor = tile();
        anchor.getStyleClass().add("anchor");
        return anchor;
    }

}
