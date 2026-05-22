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

public final class TileAnimator {

    private record LocationTypes(List<TileLocation.OnWall> onWalls,
                                List<TileLocation.OffBoard> offBoards,
                                List<TileLocation.OnSource> onSources,
                                List<TileLocation.OnFloor> onFloors,
                                List<TileLocation.OnPattern> onPatterns){
        public LocationTypes(){
            this(new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
        }
    }

    public static Animation animateTiles (Function<TileLocation, Point2D> whereToPlaceTile, Map<TileKind, List<Node>> TileNodesMap, ReadOnlyGameState gameState){


        ParallelTransition transition = new ParallelTransition();

        //remplir la demande
        //commencons par onSources
        for(TileKind kind : TileKind.ALL){
            LocationTypes kindOffre = new LocationTypes();
            LocationTypes kindDemande = new LocationTypes();
            Map<TileLocation, TileLocation> transfer = new HashMap<>();


                ReadOnlyIntArray pkTileSOurces = gameState.pkTileSources();
            int[] tileArray = pkTileSOurces.toArray();
            for(int i = 0; i < tileArray.length ; i++){
                    int numTiles = PkTileSet.countOf(tileArray[i], kind);

                    int precedantNumTiles = previousNumTiles(tileArray[i], kind);

                    for(int j = 0; j < numTiles; j++) {
                        kindDemande.onSources().add(new TileLocation.OnSource(TileSource.ALL.get(i), j + precedantNumTiles));
                }
            }
            //chaque joueur
            for(PlayerId id : gameState.game().playerIds()) {
                ReadOnlyIntArray playerStates = gameState.pkPlayerStates();

                //patterns
                int pkPattern = PkPlayerStates.pkPatterns(playerStates, id);
                for (TileDestination.Pattern line : TileDestination.Pattern.ALL){
                    TileKind.Colored colored = PkPatterns.color(pkPattern, line);

                    if (colored == kind){
                        int s = PkPatterns.size(pkPattern, line);

                        for (int a = 0; a < s; a++) {
                            kindDemande.onPatterns().add(new TileLocation.OnPattern(id, line, a));
                        }
                    }
                }

                //floors
//                int pkTileSetOfFloor = PkFloor.asPkTileSet(PkPlayerStates.pkFloor(playerStates, id));
//                int floorSize = PkTileSet.countOf(pkTileSetOfFloor, kind);
//
//                int currentFloorIndex = previousNumTiles(pkTileSetOfFloor, kind);
//
//                for (int floorIndex = 0; floorIndex < floorSize; floorIndex++) {
//                    kindDemande.onFloors().add(new TileLocation.OnFloor(id, floorIndex + currentFloorIndex));
//                }
                    int currentPkFloor = PkPlayerStates.pkFloor(playerStates, id);
                for(int i = 0; i < PkFloor.size(currentPkFloor); i++){
                        TileKind tileInFloor = PkFloor.tileAt(currentPkFloor, i);

                        if(tileInFloor == kind){
                            kindDemande.onFloors.add(new TileLocation.OnFloor(id, i));
                        }

                }

                // walls
                if (kind instanceof TileKind.Colored coloredKind) {
                    int pkWall = PkPlayerStates.pkWall(playerStates, id);

                    for(TileDestination.Pattern line : TileDestination.Pattern.ALL){
                        if(PkWall.hasTileAt(pkWall, line, coloredKind)){
                            kindDemande.onWalls().add(new TileLocation.OnWall(id, line, coloredKind));
                        }
                    }
                }

            }

            //TODO may be problematic in case of FPM not showing

                int pkBag = gameState.pkTileBag();
                int pkDiscarded = gameState.pkDiscardedTiles();
                int pkAllOutOfGame = PkTileSet.union(pkBag, pkDiscarded);
                int numOffBoard = PkTileSet.countOf(pkAllOutOfGame, kind);
                for (int b = 0; b < numOffBoard; b++) {
                    kindDemande.offBoards().add(new TileLocation.OffBoard(kind, b));
                }

            List<Node> kindNodes = TileNodesMap.get(kind);

                //remplir kindOffre
            for (Node node : kindNodes){


                TileLocation currentLoc = Tiles.location(node);
                switch (currentLoc) {
                    case TileLocation.OnFloor floor -> {
                        if (kindDemande.onFloors().contains(floor)) {
                            kindDemande.onFloors().remove(floor);
                        } else {
                            kindOffre.onFloors().add(floor);
                        }
                    }
                    case TileLocation.OnSource source -> {
                        if (kindDemande.onSources().contains(source)) {
                            kindDemande.onSources().remove(source);
                        } else {
                            kindOffre.onSources().add(source);
                        }
                    }
                    case TileLocation.OnWall wall -> {
                        if (kindDemande.onWalls().contains(wall)) {
                            kindDemande.onWalls().remove(wall);
                        } else {
                            kindOffre.onWalls().add(wall);
                        }
                    }
                    case TileLocation.OnPattern pattern -> {
                        if (kindDemande.onPatterns().contains(pattern)) {
                            kindDemande.onPatterns().remove(pattern);
                        } else {
                            kindOffre.onPatterns().add(pattern);
                        }
                    }
//                    case TileLocation.OffBoard offBoard -> {
//                        if (kindDemande.offBoards().contains(offBoard)) {
//                            kindDemande.offBoards().remove(offBoard);
//                        } else {
//                            kindOffre.offBoards().add(offBoard);
//                        }
                    case TileLocation.OffBoard offBoard -> {
                        if (!kindDemande.offBoards().isEmpty()) {
                            kindDemande.offBoards().removeFirst();
                        } else {
                            kindOffre.offBoards().add(offBoard);
                        }
                    }
                }
            }
            // trier les sources
            kindOffre.onSources().sort(Comparator.comparing((TileLocation.OnSource s) -> s.source().index()).reversed().thenComparing(TileLocation.OnSource::index));
            //algorithme
            //todo OURS
            //pour boucler, puis retirer je dois faire une copie.
            for(TileLocation.OnWall destination : new ArrayList<>(kindDemande.onWalls())){
                TileLocation.OnPattern source = kindOffre.onPatterns().stream()
                                .filter(p ->
                                    p.line() == destination.line() &&
                                    p.playerId() == destination.playerId())
                                .max(Comparator.comparingInt(TileLocation.OnPattern :: index))
                                .orElseThrow();

                kindOffre.onPatterns().remove(source);
                transfer.put(source, destination);

                //todo modifié
                kindDemande.onWalls().remove(destination);
            }
//            for(TileLocation.OnFloor destination : kindDemande.onFloors()){
//                TileLocation.OnSource source = kindOffre.onSources().removeFirst();
//                transfer.put(source, destination);
//
//                //todo modifié
////                kindDemande.onFloors().remove(destination);
//            }

            while(!kindDemande.onFloors().isEmpty() && !kindOffre.onSources().isEmpty()){
                TileLocation.OnSource source = kindOffre.onSources().removeFirst();
                TileLocation.OnFloor destination = kindDemande.onFloors().removeFirst();
                transfer.put(source, destination);
            }

            //on a problème ici
            //SOIT on a pas assez de choses dans kindOFfre
            //SOIT on a trop de chose dans kindDemande
            //=> kind offre ou kind demande quand on remplis
//            for (TileLocation.OnPattern destination : kindDemande.onPatterns()) {
//                TileLocation.OnSource source = kindOffre.onSources().removeFirst();
//                transfer.put(source, destination);
//
//                //todo modifié
////                kindDemande.onPatterns().remove(destination);
//            }

            while(!kindDemande.onPatterns().isEmpty() && !kindOffre.onSources().isEmpty()){
                TileLocation.OnSource source = kindOffre.onSources().removeFirst();
                TileLocation.OnPattern destination = kindDemande.onPatterns().removeFirst();
                transfer.put(source, destination);
            }



//            for (TileLocation.OffBoard destination : kindDemande.offBoards()) {
//                if (!kindOffre.onFloors().isEmpty() ) {
//                    transfer.put(kindOffre.onFloors().removeFirst(), destination);
//
//                    //todo modifié
////                    kindDemande.offBoards().remove(destination);
//                }
////                } else if (!kindOffre.onSources().isEmpty()) {
////                    transfer.put(kindOffre.onSources().removeFirst(), destination);
////                }
//            }
            while(!kindDemande.offBoards().isEmpty() && !kindOffre.onFloors().isEmpty()){
                TileLocation.OffBoard destination = kindDemande.offBoards().removeFirst();
                TileLocation.OnFloor source = kindOffre.onFloors().removeFirst();
                transfer.put(source, destination);
            }

//            for (TileLocation.OffBoard destination : kindDemande.offBoards()) {
//                if(!kindOffre.onSources().isEmpty()){
//                    transfer.put(kindOffre.onSources().removeFirst(), destination);
//
//                    //todo modifié
////                    kindDemande.offBoards().remove(destination);
//                }
//            }

            while(!kindDemande.offBoards().isEmpty() && !kindOffre.onSources().isEmpty()){
                TileLocation.OffBoard destination = kindDemande.offBoards().removeFirst();
                TileLocation.OnSource source = kindOffre.onSources().removeFirst();
                transfer.put(source, destination);
            }
//            while (!kindDemande.offBoards().isEmpty() && !kindOffre.onFloors().isEmpty()) {
//                transfer.put(kindOffre.onFloors().removeFirst(), kindDemande.offBoards().removeFirst());
//            }
//            while (!kindDemande.offBoards().isEmpty() && !kindOffre.onSources().isEmpty()) {
//                transfer.put(kindOffre.onSources().removeFirst(), kindDemande.offBoards().removeFirst());
//            }

            List<TileLocation> resteOffre = new ArrayList<>();
            resteOffre.addAll(kindOffre.offBoards());
            resteOffre.addAll(kindOffre.onFloors());
            resteOffre.addAll(kindOffre.onSources());
            resteOffre.addAll(kindOffre.onPatterns());
            resteOffre.addAll(kindOffre.onWalls());

            List<TileLocation> resteDemande = new ArrayList<>();
            resteDemande.addAll(kindDemande.offBoards());
            resteDemande.addAll(kindDemande.onFloors());
            resteDemande.addAll(kindDemande.onSources());
            resteDemande.addAll(kindDemande.onPatterns());
            resteDemande.addAll(kindDemande.onWalls());

            for (int k = 0; k < resteOffre.size(); k++) {
                transfer.put(resteOffre.get(k), resteDemande.get(k));
            }


//            // algorithme todo geminis
//
//// 1. Mur (Cas spécial, on doit chercher le bon pattern)
//// On fait une copie de la liste pour pouvoir modifier l'originale sans crasher
//            for(TileLocation.OnWall destination : new ArrayList<>(kindDemande.onWalls())){
//                TileLocation.OnPattern source = kindOffre.onPatterns().stream()
//                        .filter(p -> p.line() == destination.line() && p.playerId() == destination.playerId())
//                        .max(Comparator.comparingInt(TileLocation.OnPattern::index))
//                        .orElseThrow();
//
//                kindOffre.onPatterns().remove(source);
//                kindDemande.onWalls().remove(destination); // 🛠️ On vide la demande !
//                transfer.put(source, destination);
//            }
//
//// 2. Plancher
//            while(!kindDemande.onFloors().isEmpty() && !kindOffre.onSources().isEmpty()){
//                transfer.put(kindOffre.onSources().removeFirst(), kindDemande.onFloors().removeFirst());
//            }
//
//// 3. Lignes de motif
//            while(!kindDemande.onPatterns().isEmpty() && !kindOffre.onSources().isEmpty()){
//                transfer.put(kindOffre.onSources().removeFirst(), kindDemande.onPatterns().removeFirst());
//            }
//
//// 4. Hors du plateau -> Plancher
//            while(!kindDemande.offBoards().isEmpty() && !kindOffre.onFloors().isEmpty()){
//                transfer.put(kindOffre.onFloors().removeFirst(), kindDemande.offBoards().removeFirst());
//            }
//
//// 5. Hors du plateau -> Sources
//            while(!kindDemande.offBoards().isEmpty() && !kindOffre.onSources().isEmpty()){
//                transfer.put(kindOffre.onSources().removeFirst(), kindDemande.offBoards().removeFirst());
//            }
//
//// 6. Reste (Les reliquats s'associent entre eux)
//            List<TileLocation> resteOffre = new ArrayList<>();
//            resteOffre.addAll(kindOffre.offBoards());
//            resteOffre.addAll(kindOffre.onFloors());
//            resteOffre.addAll(kindOffre.onSources());
//            resteOffre.addAll(kindOffre.onPatterns());
//            resteOffre.addAll(kindOffre.onWalls());
//
//            List<TileLocation> resteDemande = new ArrayList<>();
//            resteDemande.addAll(kindDemande.offBoards());
//            resteDemande.addAll(kindDemande.onFloors());
//            resteDemande.addAll(kindDemande.onSources());
//            resteDemande.addAll(kindDemande.onPatterns());
//            resteDemande.addAll(kindDemande.onWalls());
//
//
//
//// Maintenant, on est sûr que les restes ont la même taille et sont propres !
//            for (int k = 0; k < resteOffre.size(); k++) {
//                transfer.put(resteOffre.get(k), resteDemande.get(k));
//            }



            Duration duration = new Duration(500);
            //Une ParallelTransition est composee de 3 choses:
            //
            for(TileLocation key : transfer.keySet()){
                Node nodeFirstLocation = TileNodesMap.get(kind).stream().filter(n -> Tiles.location(n).equals(key)).findFirst().orElseThrow();

                Tiles.setLocation(nodeFirstLocation, transfer.get(key));

                transition.getChildren().add(new RelocationTransition( nodeFirstLocation, whereToPlaceTile.apply(transfer.get(key)), duration));
            }
        }
        return transition;
    }

    private static int previousNumTiles(int pkTileSet,TileKind currentKind){
        int precedantNumTiles = 0;
        for(TileKind tileKind : TileKind.ALL){
            int colorCount = PkTileSet.countOf(pkTileSet, tileKind);
            if(tileKind == currentKind){
                break;
            }
            precedantNumTiles += colorCount;
        }
        return precedantNumTiles;
    }


}
