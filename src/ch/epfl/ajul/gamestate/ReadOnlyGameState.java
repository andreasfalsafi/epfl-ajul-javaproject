package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

/**
 * Représente un état de partie d'Ajul en lecture seule.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public interface ReadOnlyGameState {

    /**
     * Retourne la configuration de la partie.
     *
     * @return la configuration de la partie
     */
     Game game();

    /**
     * Retourne le contenu du sac duquel les tuiles sont extraites pour remplir les fabriques,
     * sous la forme d'un ensemble de tuiles empaqueté.
     *
     * @return le contenu empaqueté du sac
     */
     int pkTileBag();

    /**
     * Retourne un tableau décrivant le contenu des sources de tuiles, l'élément à l'index i
     * de ce tableau étant un ensemble de tuiles empaqueté correspondant à la source d'index i.
     *
     * @return le tableau en lecture seule des sources de tuiles empaquetées
     */
     ReadOnlyIntArray pkTileSources();

    /**
     * Retourne l'ensemble empaqueté des index des sources uniques.
     *
     * @return l'ensemble empaqueté des index des sources uniques
     */
     int pkUniqueTileSources();

    /**
     * Retourne un tableau contenant les états empaquetés des joueurs.
     *
     * @return le tableau en lecture seule des états empaquetés des joueurs
     */
     ReadOnlyIntArray pkPlayerStates();

    /**
     * Retourne l'identité du joueur courant.
     *
     * @return l'identité du joueur courant
     */
     PlayerId currentPlayerId();

    /**
     * Retourne une version immuable de l'état de la partie auquel on l'applique.
     *
     * @return une version immuable de l'état de la partie
     */
     default ImmutableGameState immutable(){
         return new ImmutableGameState(game(), pkTileBag(), ImmutableIntArray.copyOf(pkTileSources().toArray()), pkUniqueTileSources(), ImmutableIntArray.copyOf(pkPlayerStates().toArray()), currentPlayerId());
     }

    /**
     * Retourne la liste des identités des joueurs de la partie.
     *
     * @return la liste des identités des joueurs
     */
     default List<PlayerId> playerIds(){
         return game().playerIds();
     }

    /**
     * Retourne vrai si et seulement si la manche est terminée
     * (c.-à-d. qu'aucune source de tuile ne contient de tuile colorée).
     *
     * @return true si la manche est terminée, false sinon
     */
     default boolean isRoundOver(){
         return (pkUniqueTileSources() == PkIntSet32.EMPTY);
     }

    /**
     * Retourne vrai si et seulement si la partie est terminée
     * (c.-à-d. que la manche est terminée et qu'au moins un joueur possède une ligne horizontale complète).
     *
     * @return true si la partie est terminée, false sinon
     */
     default boolean isGameOver(){

         if(isRoundOver()){
             for (PlayerId playerId : playerIds()){
                 if (PkWall.hasFullRow(PkPlayerStates.pkWall(pkPlayerStates(), playerId))){
                     return true;
                 }
             }
         }

         return false;
     }

    /**
     * Retourne l'ensemble empaqueté des tuiles sorties du jeu.
     *
     * @return l'ensemble empaqueté des tuiles défaussées
     */
     default int pkDiscardedTiles(){
         int calculatedPkOutOfGame = PkTileSet.FULL;
         //bag
         calculatedPkOutOfGame = PkTileSet.difference(calculatedPkOutOfGame, pkTileBag() );
         //fabrique et hopefuly zone centrale
         for(int i = 0; i < game().tileSourcesCount(); i++) {
             calculatedPkOutOfGame = PkTileSet.difference(calculatedPkOutOfGame,pkTileSources().get(i));
         }
         //pkPatterns, pkWall, pkFloor
         for(PlayerId playerId : playerIds()){
             int currentWall = PkWall.asPkTileSet(PkPlayerStates.pkWall(pkPlayerStates(),playerId));
             calculatedPkOutOfGame = PkTileSet.difference(calculatedPkOutOfGame,currentWall);

             int currentPattern = PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(pkPlayerStates(),playerId));
             calculatedPkOutOfGame = PkTileSet.difference(calculatedPkOutOfGame,currentPattern);

             int currentFloor = PkFloor.asPkTileSet(PkPlayerStates.pkFloor(pkPlayerStates(),playerId));
             calculatedPkOutOfGame = PkTileSet.difference(calculatedPkOutOfGame,currentFloor);
         }
         return calculatedPkOutOfGame;
     }


    /**
     * Méthode utilitaire privée générant les coups valides, avec la possibilité
     * de filtrer uniquement sur les sources uniques.
     *
     * @param destination le tableau dans lequel placer les coups empaquetés
     * @param isUnique true pour ne considérer que les sources uniques, false sinon
     * @return le nombre de coups valides placés dans le tableau
     */
    private int validMoveGeneral(short[] destination, boolean isUnique) {
        Preconditions.checkArgument(destination.length >= Move.MAX_MOVES);
        int indexInTab = 0;

        if (!isRoundOver()) {
            int currentPkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());
            int currentPkWall = PkPlayerStates.pkWall(pkPlayerStates(), currentPlayerId());

            for (int i = 0; i < game().tileSourcesCount(); i++) {
                int currentPkTileSource;
                TileSource currentSource;

                currentPkTileSource = pkTileSources().get(i);
                currentSource = TileSource.ALL.get(i);

                if (isUnique && !PkIntSet32.contains(pkUniqueTileSources(), i)) {
                    continue;
                }

                if (PkTileSet.isEmpty(currentPkTileSource)) {
                    continue;
                }

                for (TileKind.Colored colored : TileKind.Colored.ALL) {
                    if ((PkTileSet.countOf(currentPkTileSource, colored) <= 0)) {
                        continue;
                    }

                    destination[indexInTab] = PkMove.pack(currentSource, colored, TileDestination.FLOOR);
                    indexInTab++;
                    for (TileDestination.Pattern goal : TileDestination.Pattern.ALL) {
                        if (canBePutHere(currentPkPatterns, currentPkWall, goal, colored)) {

                            destination[indexInTab] = PkMove.pack(currentSource, colored, goal);
                            indexInTab++;
                        }
                    }
                }
            }
        }
        return indexInTab;
    }

     private static boolean canBePutHere(int pkPattern, int pkWallPlayer, TileDestination.Pattern goal, TileKind.Colored color){
         return PkPatterns.canContain(pkPattern, goal, color)
                 && !PkPatterns.isFull(pkPattern,goal)
                 && !PkWall.hasTileAt(pkWallPlayer, goal, color);
     }
    /**
     * Place dans le tableau donné tous les coups empaquetés que le joueur courant
     * pourrait jouer, et retourne leur nombre.
     *
     * @param destination le tableau devant accueillir les coups empaquetés
     * @return le nombre de coups valides générés
     */
     default int validMoves(short[] destination) {
         return validMoveGeneral(destination, false);
     }

    /**
     * Place dans le tableau donné tous les coups empaquetés que le joueur courant
     * pourrait jouer en ne considérant que les sources uniques, et retourne leur nombre.
     *
     * @param destination le tableau devant accueillir les coups empaquetés
     * @return le nombre de coups valides uniques générés
     */
     default int uniqueValidMoves(short[] destination){
         return validMoveGeneral(destination, true);
     }
}
