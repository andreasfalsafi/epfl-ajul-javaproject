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


//créer une classe statique impriquée échantillonneur par réservoir. cette classe choisi aléàeatoirement de prendre la valeur qu'on lui add
//trouver les coups qui remplissent totalement une ligne de motif
// 1) LA QTY DE TUILES DE COULEURS
//2) LA DESTINATION OÛ ELLES VONT
//3) LA TAILLE QUI RESTE (CAPACITY-SIZE)
//4) SI QTY==SZELEFT => group1
//5) si qty < sizeleft => group2
//6) else group 3
public final class HeuristicMoveSelector {

    private static final class ReservoirSampler {
        private int currentElem = -1;
        private int countElem = 0;

        public ReservoirSampler(){
        }

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

    //todo on peut simplifier en faisant les deux premiers if seulement si on est PAS dans fllor, et tout mettre dans le 3ème sinon.
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
