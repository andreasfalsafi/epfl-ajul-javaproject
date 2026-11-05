package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Function;

/**
 * Classe utilitaire gérant l'animation du déplacement des tuiles sur le plateau de jeu.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class TileAnimator {

    public static final int ANIMATION_DURATION = 500;

    private record LocationTypes(List<TileLocation.OnWall> onWalls,
                                 List<TileLocation.OffBoard> offBoards,
                                 List<TileLocation.OnSource> onSources,
                                 List<TileLocation.OnFloor> onFloors,
                                 List<TileLocation.OnPattern> onPatterns) {
        public LocationTypes() {
            this(new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
        }
    }

    /**
     * Détermine les déplacements nécessaires pour synchroniser l'affichage graphique avec l'état logique de la partie.
     */
    public static Animation animateTiles(Function<TileLocation, Point2D> whereToPlaceTile, Map<TileKind, List<Node>> tileNodesMap, ReadOnlyGameState gameState) {
        ParallelTransition transition = new ParallelTransition();

        for (TileKind kind : TileKind.ALL) {
            LocationTypes kindSupply = new LocationTypes();
            LocationTypes kindDemand = new LocationTypes();
            Map<TileLocation, TileLocation> transfer = new HashMap<>();

            List<Node> kindNodes = setDemand(tileNodesMap, gameState, kind, kindDemand);
            setSupply(kindNodes, kindDemand, kindSupply);
            pairingAlgorithm(kindDemand, kindSupply, transfer);
            createAnimation(whereToPlaceTile, tileNodesMap, kind, transfer, transition);
        }
        return transition;
    }

    private static List<Node> setDemand(Map<TileKind, List<Node>> tileNodesMap, ReadOnlyGameState gameState, TileKind kind, LocationTypes kindDemand) {
        ReadOnlyIntArray pkTileSources = gameState.pkTileSources();

        // Sources
        int[] tileArray = pkTileSources.toArray();
        for (int i = 0; i < tileArray.length; i++) {
            int numTiles = PkTileSet.countOf(tileArray[i], kind);
            int precedantNumTiles = previousNumTiles(tileArray[i], kind);

            for (int j = 0; j < numTiles; j++) {
                kindDemand.onSources().add(new TileLocation.OnSource(TileSource.ALL.get(i), j + precedantNumTiles));
            }
        }

        for (PlayerId id : gameState.game().playerIds()) {
            ReadOnlyIntArray playerStates = gameState.pkPlayerStates();

            // Patterns
            int pkPattern = PkPlayerStates.pkPatterns(playerStates, id);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                TileKind.Colored colored = PkPatterns.color(pkPattern, line);

                if (colored == kind) {
                    int s = PkPatterns.size(pkPattern, line);
                    for (int a = 0; a < s; a++) {
                        kindDemand.onPatterns().add(new TileLocation.OnPattern(id, line, a));
                    }
                }
            }

            // Floors
            int currentPkFloor = PkPlayerStates.pkFloor(playerStates, id);
            for (int i = 0; i < PkFloor.size(currentPkFloor); i++) {
                TileKind tileInFloor = PkFloor.tileAt(currentPkFloor, i);

                if (tileInFloor == kind) {
                    kindDemand.onFloors().add(new TileLocation.OnFloor(id, i));
                }
            }

            // Walls
            if (kind instanceof TileKind.Colored coloredKind) {
                int pkWall = PkPlayerStates.pkWall(playerStates, id);

                for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                    if (PkWall.hasTileAt(pkWall, line, coloredKind)) {
                        kindDemand.onWalls().add(new TileLocation.OnWall(id, line, coloredKind));
                    }
                }
            }
        }

        // OffBoard
        int pkBag = gameState.pkTileBag();
        int pkDiscarded = gameState.pkDiscardedTiles();
        int pkAllOutOfGame = PkTileSet.union(pkBag, pkDiscarded);
        int numOffBoard = PkTileSet.countOf(pkAllOutOfGame, kind);
        for (int b = 0; b < numOffBoard; b++) {
            kindDemand.offBoards().add(new TileLocation.OffBoard(kind, b));
        }

        return tileNodesMap.get(kind);
    }

    private static int previousNumTiles(int pkTileSet, TileKind currentKind) {
        int count = 0;
        for (TileKind tileKind : TileKind.ALL) {
            if (tileKind == currentKind) break;
            count += PkTileSet.countOf(pkTileSet, tileKind);
        }
        return count;
    }

    private static void setSupply(List<Node> kindNodes, LocationTypes kindDemand, LocationTypes kindSupply) {
        for (Node node : kindNodes) {
            TileLocation currentLoc = Tiles.location(node);
            switch (currentLoc) {
                case TileLocation.OnFloor floor ->
                        processLocation(floor, kindDemand.onFloors(), kindSupply.onFloors());

                case TileLocation.OnSource source ->
                        processLocation(source, kindDemand.onSources(), kindSupply.onSources());

                case TileLocation.OnWall wall ->
                        processLocation(wall, kindDemand.onWalls(), kindSupply.onWalls());

                case TileLocation.OnPattern pattern ->
                        processLocation(pattern, kindDemand.onPatterns(), kindSupply.onPatterns());

                case TileLocation.OffBoard offBoard ->
                        processLocation(offBoard, kindDemand.offBoards(), kindSupply.offBoards());
            }
        }
    }

    private static <T extends TileLocation> void processLocation(T loc, List<T> demandList, List<T> supplyList) {
        if (demandList.contains(loc)) {
            demandList.remove(loc);
        } else {
            supplyList.add(loc);
        }
    }

    private static void pairingAlgorithm(LocationTypes kindDemand, LocationTypes kindSupply, Map<TileLocation, TileLocation> transfer) {
        kindSupply.onSources().sort(Comparator.comparing((TileLocation.OnSource s) ->
                s.source().index()).reversed().thenComparing(TileLocation.OnSource::index));

        for (TileLocation.OnWall destination : new ArrayList<>(kindDemand.onWalls())) {
            TileLocation.OnPattern source = kindSupply.onPatterns().stream()
                    .filter(p -> p.line() == destination.line() && p.playerId() == destination.playerId())
                    .max(Comparator.comparingInt(TileLocation.OnPattern::index))
                    .orElseThrow();

            kindSupply.onPatterns().remove(source);
            transfer.put(source, destination);
            kindDemand.onWalls().remove(destination);
        }

        matchAndTransfer(kindSupply.onSources(), kindDemand.onFloors(), transfer);
        matchAndTransfer(kindSupply.onSources(), kindDemand.onPatterns(), transfer);
        matchAndTransfer(kindSupply.onFloors(), kindDemand.offBoards(), transfer);
        matchAndTransfer(kindSupply.onSources(), kindDemand.offBoards(), transfer);

        List<TileLocation> resteSupply = gatherRemaining(kindSupply);
        List<TileLocation> resteDemand = gatherRemaining(kindDemand);

        for (int k = 0; k < resteSupply.size(); k++) {
            transfer.put(resteSupply.get(k), resteDemand.get(k));
        }
    }

    private static <S extends TileLocation, D extends TileLocation> void matchAndTransfer(List<S> supply, List<D> demand, Map<TileLocation, TileLocation> transfer) {
        while (!demand.isEmpty() && !supply.isEmpty()) {
            transfer.put(supply.removeFirst(), demand.removeFirst());
        }
    }

    private static List<TileLocation> gatherRemaining(LocationTypes locs) {
        List<TileLocation> all = new ArrayList<>();
        all.addAll(locs.offBoards());
        all.addAll(locs.onFloors());
        all.addAll(locs.onSources());
        all.addAll(locs.onPatterns());
        all.addAll(locs.onWalls());
        return all;
    }

    private static void createAnimation(Function<TileLocation, Point2D> whereToPlaceTile, Map<TileKind, List<Node>> tileNodesMap, TileKind kind, Map<TileLocation, TileLocation> transfer, ParallelTransition transition) {
        Duration duration = new Duration(ANIMATION_DURATION);
        for (TileLocation key : transfer.keySet()) {
            Node nodeFirstLocation = tileNodesMap.get(kind).stream()
                    .filter(n -> Tiles.location(n).equals(key))
                    .findFirst()
                    .orElseThrow();

            Tiles.setLocation(nodeFirstLocation, transfer.get(key));
            transition.getChildren().add(new RelocationTransition(nodeFirstLocation, whereToPlaceTile.apply(transfer.get(key)), duration));
        }
    }
}