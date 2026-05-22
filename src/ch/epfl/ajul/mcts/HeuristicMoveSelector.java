package ch.epfl.ajul.mcts;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;


/**
 * Sélecteur de coups utilisant une approche heuristique pour le choix du coup à jouer.
 * Cette classe partitionne les coups valides en trois groupes basés sur leur capacité
 * à compléter des lignes de motif, afin de favoriser un jeu quasi-aléatoire mais structuré.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class HeuristicMoveSelector {

    /**
     * Échantillonneur par réservoir permettant de sélectionner uniformément un élément
     * parmi un flux d'éléments de taille inconnue ou dynamique.
     */
    private static final class ReservoirSampler {
        private int currentElem = -1;
        private int countElem = 0;


        public ReservoirSampler(){
        }

        /**
         * Ajoute un élément au réservoir avec une probabilité {@code 1/n}.
         * @param elem l'index du coup à ajouter
         * @param random le générateur de nombres aléatoires
         */
        private void add(int elem, RandomGenerator random){
            countElem++;
            if (random.nextInt(countElem) == 0) {
                currentElem = elem;
            }
        }

        private int getCurrentElem(){
            return currentElem;
        }

        private boolean isEmpty(){
            return currentElem == -1;
        }
    }

    /**
     * Sélectionne un coup parmi les coups valides fournis.
     * Le choix se fait en priorité parmi :
     * 1. Les coups remplissant exactement une ligne de motif.
     * 2. Les coups remplissant partiellement une ligne de motif.
     * 3. Les coups déposés sur le plancher ou les coups excédentaires.
     *
     * @param random      le générateur de nombres aléatoires
     * @param gameState   l'état de la partie en lecture seule
     * @param validMoves  tableau des coups valides empaquetés
     * @param numValidMoves nombre de coups valides dans le tableau
     * @return l'index du coup choisi dans le tableau {@code validMoves}
     */
    public static int selectMove(RandomGenerator random, ReadOnlyGameState gameState, short[] validMoves, int numValidMoves ){
        ReservoirSampler array1 = new ReservoirSampler();
        ReservoirSampler array2 = new ReservoirSampler();
        ReservoirSampler array3 = new ReservoirSampler();

        int currentPkPattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());

        for(int i = 0; i < numValidMoves; i++) {
            short currentMove = validMoves[i];
            TileSource source = PkMove.source(currentMove);
            TileKind color = PkMove.color(currentMove);

            if (PkMove.destination(currentMove) == TileDestination.FLOOR) {
                array3.add(i, random);
            } else {
                TileDestination.Pattern destination = (TileDestination.Pattern) PkMove.destination(currentMove);
                int pkTileSetSource = gameState.pkTileSources().get(source.index());
                int numTiles = PkTileSet.countOf(pkTileSetSource, color);

                int sizeLeft = destination.capacity() - PkPatterns.size(currentPkPattern, destination);

                if (sizeLeft == numTiles) {
                    array1.add(i, random);
                } else if (numTiles < sizeLeft) {
                    array2.add(i, random);
                } else {
                    array3.add(i, random);
                }
            }
        }

        if(!(array1.isEmpty())){
            return array1.getCurrentElem();
        }
        else if (!(array2.isEmpty())){
            return array2.getCurrentElem();
        }
        return array3.getCurrentElem();
    }
}
