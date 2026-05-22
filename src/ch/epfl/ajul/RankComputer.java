package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.Arrays;
import java.util.List;

/**
 * Classe utilitaire permettant de calculer le classement (rang) des joueurs
 * à la fin d'une partie d'Ajul.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class RankComputer {

    /**
     * Calcule le rang de chaque joueur à la fin d'une partie et remplit le
     * tableau {@code ranking} avec ces valeurs.
     * * Le rang est déterminé par le nombre de points (critère principal)
     * suivi du nombre de lignes complètes dans le mur (critère secondaire).
     * Les joueurs ayant des scores identiques reçoivent le même rang.
     *
     * @param gameState l'état de la partie en lecture seule
     * @param ranking   le tableau d'entiers à remplir avec les rangs des joueurs
     */
    public static void playersRank(ReadOnlyGameState gameState, int[] ranking){
        int[] order = new int[ranking.length];

        List<PlayerId> playerIds = gameState.playerIds();
        ReadOnlyIntArray pkPlayerStates =gameState.pkPlayerStates();
        for (int i = 0; i < playerIds.size(); i++) {

            int playerPoints = PkPlayerStates.points(pkPlayerStates, playerIds.get(i));
            int numFullLines = 0;
            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL){
                if (PkWall.isRowFull(PkPlayerStates.pkWall(pkPlayerStates, playerIds.get(i)), pattern)){
                    numFullLines++;
                }
            }

            order[i] = (playerPoints << 5) | (numFullLines << 2) | i;

        }

        Arrays.sort(order);


        int currRanking = 0;
        int lastElem = order.length-1;
        int scorefirst = order[lastElem]>>2;

        for(int i = 0; i < ranking.length; i++){
            int j = order[lastElem - i] & 0b11;
            if((order[lastElem - i] >> 2) == scorefirst) {
                ranking[j] = currRanking;
            }
            else{
                currRanking = i;
                ranking[j] = currRanking;
                scorefirst = (order[lastElem - i] >> 2);
            }
        }
    }


}
