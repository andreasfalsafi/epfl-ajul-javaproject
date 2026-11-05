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
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class RankComputer {

    public static final int LINE_SCORE_SHIFT = 2;
    public static final int PLAYER_POINTS_SHIFT = 5;
    public static final int PLAYER_NUMBER_MASK = 0b11;

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
    public static void playersRank(ReadOnlyGameState gameState, int[] ranking) {
        int[] order = new int[ranking.length];

        List<PlayerId> playerIds = gameState.playerIds();
        ReadOnlyIntArray pkPlayerStates = gameState.pkPlayerStates();

        //prendre le nombre de points et lignes pour chaque joueur
        for (int i = 0; i < playerIds.size(); i++) {

            int playerPoints = PkPlayerStates.points(pkPlayerStates, playerIds.get(i));
            int numFullLines = 0;
            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(PkPlayerStates.pkWall(pkPlayerStates, playerIds.get(i)), pattern)) {
                    numFullLines++;
                }
            }
            //calculer le playerRankingCorrespondant
            order[i] = (playerPoints << PLAYER_POINTS_SHIFT) | (numFullLines << LINE_SCORE_SHIFT) | i;

        }

        Arrays.sort(order);

        reverseArray(order);

        int currRanking = 0;

        int scorefirst = order[0] >> LINE_SCORE_SHIFT;

        for (int i = 0; i < ranking.length; i++) {

            int currentPlayer = order[i] & PLAYER_NUMBER_MASK;

            int playerScore = order[i] >> LINE_SCORE_SHIFT;

            if (playerScore < scorefirst) {
                currRanking = i;
                scorefirst = playerScore;
            }
            ranking[currentPlayer] = currRanking;
        }
    }


    private static void reverseArray(int[] array) {

        for (int firstPlace = 0; firstPlace < array.length / 2; firstPlace++) {
            int lastPlace = array.length - 1 - firstPlace;
            int swappedFront = array[firstPlace];
            int swappedEnd = array[lastPlace];
            array[firstPlace] = swappedEnd;
            array[lastPlace] = swappedFront;
        }
    }

}
