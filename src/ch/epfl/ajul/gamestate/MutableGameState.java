package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.Arrays;
import java.util.random.RandomGenerator;


/**
 * Représente l'état non immuable (modifiable) d'une partie d'Ajul.
 * Permet de faire évoluer l'état du jeu en fonction des coups joués par les joueurs
 * et de la progression des manches.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class MutableGameState implements ReadOnlyGameState {

    private final Game game;
    private int pkTileBag;
    private final int[] pkTileSources;
    private int pkUniqueTileSources;
    private final int[] pkPlayerStates;
    private PlayerId currentPlayerId;
    private final PointsObserver pointsObserver;

    /**
     * Construit un état de partie modifiable à partir d'un état initial et d'un observateur de points.
     *
     * @param initialState   l'état initial de la partie (en lecture seule)
     * @param pointsObserver l'observateur de points à informer lors des changements de score
     */
    public MutableGameState(ReadOnlyGameState initialState, PointsObserver pointsObserver) {
        this.game = initialState.game();
        this.pkTileBag = initialState.pkTileBag();
        this.pkTileSources = initialState.pkTileSources().toArray();
        this.pkUniqueTileSources = initialState.pkUniqueTileSources();
        this.pkPlayerStates = initialState.pkPlayerStates().toArray();
        this.currentPlayerId = initialState.currentPlayerId();
        this.pointsObserver = pointsObserver;
    }

    /**
     * Construit un état de partie modifiable à partir d'un état initial, avec un observateur de points vide.
     *
     * @param initialState l'état initial de la partie (en lecture seule)
     */
    public MutableGameState(ReadOnlyGameState initialState) {
        this(initialState, PointsObserver.EMPTY);
    }


    /**
     * Retourne la configuration de la partie.
     *
     * @return la configuration de la partie
     */
    @Override
    public Game game() {
        return game;
    }

    /**
     * Retourne le contenu empaqueté du sac de tuiles.
     *
     * @return l'ensemble empaqueté des tuiles présentes dans le sac
     */
    @Override
    public int pkTileBag() {
        return pkTileBag;
    }

    /**
     * Retourne un tableau en lecture seule contenant l'état empaqueté des sources de tuiles.
     *
     * @return un tableau en lecture seule des sources de tuiles
     */
    @Override
    public ReadOnlyIntArray pkTileSources() {
        return MutableIntArray.wrapping(pkTileSources);
    }

    /**
     * Retourne l'ensemble empaqueté des index des sources uniques de tuiles.
     *
     * @return l'ensemble empaqueté des index des sources uniques
     */
    @Override
    public int pkUniqueTileSources() {
        return pkUniqueTileSources;
    }

    /**
     * Retourne un tableau en lecture seule contenant l'état empaqueté de tous les joueurs.
     *
     * @return un tableau en lecture seule des états des joueurs
     */
    @Override
    public ReadOnlyIntArray pkPlayerStates() {
        return MutableIntArray.wrapping(pkPlayerStates);
    }

    /**
     * Retourne l'identité du joueur courant.
     *
     * @return l'identité du joueur dont c'est le tour
     */
    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    private int calculatePkUniqueTileSource(int[] pkTileSources) {
        int pkUniqueTileSet = PkTileSet.EMPTY;
        int currentInt;

        for (int i = 0; i < game.tileSourcesCount(); i++) {
            boolean isUnique = true;

            currentInt = pkTileSources[i];
            if(PkTileSet.countOf(currentInt, TileKind.FIRST_PLAYER_MARKER) > 0) {
                currentInt = PkTileSet.remove(currentInt, TileKind.FIRST_PLAYER_MARKER);
            }
            if (currentInt == PkTileSet.EMPTY) {
                isUnique = false;
            }
            for (int j = 0; j < i; j++) {
                int previousSource = pkTileSources[j];
                        if(PkTileSet.countOf(previousSource, TileKind.FIRST_PLAYER_MARKER) > 0){
                            previousSource = PkTileSet.remove(previousSource, TileKind.FIRST_PLAYER_MARKER);
                        }

                if (currentInt == previousSource) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                pkUniqueTileSet = PkIntSet32.add(pkUniqueTileSet, i);
            }
        }
        return pkUniqueTileSet;
    }

    /**
     * Remplit les fabriques de tuiles tirées aléatoirement depuis le sac.
     * Si le sac est vide, il est rerempli au préalable avec les tuiles sorties du jeu.
     *
     * @param randomGenerator le générateur de nombres aléatoires utilisé pour l'échantillonnage et le mélange
     */
    public void fillFactories(RandomGenerator randomGenerator) {

        int fillFactories = game.factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
        TileKind.Colored[] randomColors = new TileKind.Colored[fillFactories];
        int bagSize = PkTileSet.size(pkTileBag);
        int indexOffset = 0;
        int PkAddedTiles = PkTileSet.EMPTY;

        if (bagSize <= fillFactories) {
            indexOffset = PkTileSet.copyColoredInto(pkTileBag, randomColors);

            for (int i = 0; i < bagSize; i++) {
                PkAddedTiles = PkTileSet.add(PkAddedTiles, randomColors[i]);
            }

            this.pkTileBag = pkDiscardedTiles();
        }
        int addedTotal = PkTileSet.sampleColoredInto(pkTileBag, randomColors, indexOffset, randomGenerator);
        int pkTileSetOfRandColor = PkTileSet.EMPTY;
        int numberDrawn = Math.min(fillFactories, addedTotal);

        for (int i = 0; i < numberDrawn; i++) {
            pkTileSetOfRandColor = PkTileSet.add(pkTileSetOfRandColor, randomColors[i]);
        }

        int actualDiscarded = PkTileSet.difference(pkTileSetOfRandColor, PkAddedTiles);
        this.pkTileBag = PkTileSet.difference(pkTileBag, actualDiscarded);

        TileKind.Colored[] actualColors = Arrays.copyOf(randomColors, numberDrawn);
        TileKind.Colored.shuffle(actualColors, randomGenerator);

            for (int i = 0; i < game().factoriesCount() ; i++) {
                int currentPkFactory = PkTileSet.EMPTY;

                for (int j = 0; j < TileSource.Factory.TILES_PER_FACTORY; j++) {
                    int indexRandColor = (i) * TileSource.Factory.TILES_PER_FACTORY + j;

                    if (indexRandColor < numberDrawn) {
                        currentPkFactory = PkTileSet.add(currentPkFactory, actualColors[indexRandColor]);
                    }
                }

                int factoryIndex = TileSource.Factory.ALL.get(i).index();
                this.pkTileSources[factoryIndex] = currentPkFactory;
        }
        this.pkUniqueTileSources = calculatePkUniqueTileSource(pkTileSources);
    }


    /**
     * Modifie l'état de la partie pour prendre en compte un coup joué par le joueur courant.
     * Met à jour les sources de tuiles, le plateau du joueur (motifs et plancher),
     * et passe le tour au joueur suivant.
     *
     * @param pkMove le coup empaqueté à appliquer à l'état du jeu
     */
    public void registerMove(short pkMove) {

        ReadOnlyIntArray readOnlyPlayerState = pkPlayerStates();
        TileKind.Colored color = PkMove.color(pkMove);
        TileSource source = PkMove.source(pkMove);
        int pkSource = pkTileSources[source.index()];
        TileDestination dest = PkMove.destination(pkMove);
        int pkFloorPlayer = PkPlayerStates.pkFloor(readOnlyPlayerState, currentPlayerId);
        int pkTileDestination = PkPlayerStates.pkPatterns(readOnlyPlayerState, currentPlayerId);
        int central = pkTileSources[0];
        boolean hadFPM = source.equals(TileSource.CENTER_AREA) && PkTileSet.countOf(pkSource, TileKind.FIRST_PLAYER_MARKER) == 1;

        int tilesToFloor = PkTileSet.EMPTY;
        if (hadFPM) {
            tilesToFloor = PkTileSet.add(tilesToFloor, TileKind.FIRST_PLAYER_MARKER);
            central = PkTileSet.remove(central, TileKind.FIRST_PLAYER_MARKER);
        }
        int countColor = PkTileSet.countOf(pkSource, color);
        int pkPickedTiles = PkTileSet.of(countColor, color);

        if (dest instanceof TileDestination.Pattern line) {
           int freePlace = line.capacity() - PkPatterns.size(pkTileDestination, line);
            int numTilesAdded = Math.min(countColor, freePlace);
            int numTilesIntoFloor = countColor - numTilesAdded;

            int newPkPattern = PkPatterns.withAddedTiles(pkTileDestination, line, numTilesAdded, color);
            int tileForFloor = PkTileSet.of(numTilesIntoFloor, color);

            tilesToFloor = PkTileSet.union(tilesToFloor, tileForFloor);
            PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId, newPkPattern);
        } else {
            tilesToFloor = PkTileSet.union(tilesToFloor, pkPickedTiles);
        }

        pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer, tilesToFloor);
        PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId, pkFloorPlayer);

        if (source instanceof TileSource.Factory factory) {
            int excessTiles = PkTileSet.difference(pkSource, pkPickedTiles);
            central = PkTileSet.union(central, excessTiles);
            this.pkTileSources[factory.index()] = PkTileSet.EMPTY;
        } else {
            central = PkTileSet.difference(central, pkPickedTiles);
        }
        this.pkTileSources[0] = central;
        this.currentPlayerId = PlayerId.ALL.get((currentPlayerId.ordinal() + 1) % game.playersCount());
        this.pkUniqueTileSources = calculatePkUniqueTileSource(pkTileSources);
    }


    /**
     * Termine la manche en cours.
     * Parcourt les lignes de motif de tous les joueurs pour transférer les tuiles des lignes pleines vers les murs.
     * Calcule les points gagnés, déduit les pénalités liées au plancher, notifie l'observateur de points,
     * et réinitialise le marqueur de premier joueur au centre pour la prochaine manche.
     */
    public void endRound() {
        for (PlayerId player : game.playerIds()) {
            int pointsThisRound = 0;
            int currentWall = PkPlayerStates.pkWall(pkPlayerStates(), player);
            int thisPlayerPatterns = PkPlayerStates.pkPatterns(pkPlayerStates(), player);
            for (TileDestination.Pattern line : TileDestination.Pattern.values()) {
                if (PkPatterns.isFull(thisPlayerPatterns, line)) {
                    TileKind.Colored color = PkPatterns.color(thisPlayerPatterns, line);
                    currentWall = PkWall.withTileAt(currentWall, line, color);
                    thisPlayerPatterns = PkPatterns.withEmptyLine(thisPlayerPatterns, line);

                    int hGroupSize = PkWall.hGroupSize(currentWall, line, color);
                    int vGroupSize = PkWall.vGroupSize(currentWall, line, color);
                    int pointsForMove = Points.newWallTilePoints(hGroupSize, vGroupSize);
                    this.pointsObserver.newWallTile(player, line, color, pointsForMove);

                    pointsThisRound += pointsForMove;
                }
            }
            int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), player);
            int penalty = Points.totalFloorPenalty(PkFloor.size(pkFloorPlayer));
            int pointPlayer = PkPlayerStates.points(pkPlayerStates(), player) + pointsThisRound;


            if (penalty > 0) {
                int actualLostPoints= Math.min(pointPlayer, penalty);

                if (actualLostPoints > 0) {
                    this.pointsObserver.floor(player, actualLostPoints);
                }
            }

            pointsThisRound -= penalty;
            PkPlayerStates.addPoints(pkPlayerStates, player, pointsThisRound);


            int pkNewFloor = PkFloor.EMPTY;
            if (PkFloor.containsFirstPlayerMarker(pkFloorPlayer)) {
                currentPlayerId = player;
                pkTileSources[0] = PkTileSet.add(pkTileSources[0], TileKind.FIRST_PLAYER_MARKER);
            }
            PkPlayerStates.setPkFloor(pkPlayerStates, player, pkNewFloor);
            PkPlayerStates.setPkWall(pkPlayerStates, player, currentWall);
            PkPlayerStates.setPkPatterns(pkPlayerStates, player, thisPlayerPatterns);
        }
    }

    /**
     * Termine la partie.
     * Inspecte le mur de chaque joueur pour attribuer les points bonus liés
     * aux lignes pleines, colonnes pleines et couleurs complètes,
     * et en informe l'observateur de points.
     */
    public void endGame() {
        for (PlayerId player : game.playerIds()) {
            int currentWall = PkPlayerStates.pkWall(pkPlayerStates(), player);
            int totalPointsGained=0;

            for(TileDestination.Pattern line: TileDestination.Pattern.values()){
                if(PkWall.isRowFull(currentWall, line)){
                    int pointsGainedForLine = Points.FULL_ROW_BONUS_POINTS;
                    this.pointsObserver.fullRow(player, line, pointsGainedForLine);

                    totalPointsGained+=pointsGainedForLine;
                }
            }
            for(int column=0; column < PkWall.WALL_WIDTH; column++){
                if(PkWall.isColumnFull(currentWall, column)){
                    int pointsGainedForColumn = Points.FULL_COLUMN_BONUS_POINTS;
                    this.pointsObserver.fullColumn(player, column, pointsGainedForColumn);

                    totalPointsGained+=pointsGainedForColumn;
                }
            }
            for(TileKind.Colored color: TileKind.Colored.values()){
                if(PkWall.isColorFull(currentWall, color)){
                    int pointsGainedForcolor = Points.FULL_COLOR_BONUS_POINTS;
                    this.pointsObserver.fullColor(player, color, pointsGainedForcolor);

                    totalPointsGained+=pointsGainedForcolor;
                }
            }

            PkPlayerStates.addPoints(pkPlayerStates, player, totalPointsGained);
        }
    }
}



