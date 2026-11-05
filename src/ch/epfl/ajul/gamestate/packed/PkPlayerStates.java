package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/**
 * Contient des méthodes statiques permettant de manipuler les états empaquetés
 * de tous les joueurs d'une partie d'Ajul, stockés dans un tableau.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class PkPlayerStates {

    private static final int NUM_INFO_PER_PLAYER = 4;

    // Décalage depuis chaque joueur pour obtenir les informations correspondantes
    private static final int PATTERN_OFFSET = 0;
    private static final int FLOOR_OFFSET = 1;
    private static final int WALL_OFFSET = 2;
    private static final int POINTS_OFFSET = 3;

    /**
     * Retourne un tableau immuable contenant l'état empaqueté initial des joueurs de la partie.
     * Toutes les lignes de motif, lignes plancher et murs sont vides, et tous les points valent 0.
     *
     * @param game la configuration de la partie
     * @return le tableau immuable des états initiaux des joueurs
     */
    public static ImmutableIntArray initial(Game game){
        int[] array = new int[game.playersCount() * NUM_INFO_PER_PLAYER];
        return ImmutableIntArray.copyOf(array);
    }

    /**
     * Calcule l'index de base dans le tableau pour une information spécifique d'un joueur donné.
     *
     * @param playerId le joueur concerné
     * @param offset le décalage correspondant à l'information souhaitée (motif, plancher, mur ou points)
     * @return l'index exact dans le tableau d'états
     */
    private static int placePlayer(PlayerId playerId, int offset){
        return NUM_INFO_PER_PLAYER * playerId.ordinal() + offset;
    }

    /**
     * Récupère une information spécifique d'un joueur depuis le tableau en lecture seule.
     *
     * @param pkPlayerStates le tableau des états empaquetés en lecture seule
     * @param playerId le joueur concerné
     * @param offset le décalage de l'information souhaitée
     * @return la valeur empaquetée de l'information
     */
    private static int getPlayerInfoInArray(ReadOnlyIntArray pkPlayerStates, PlayerId playerId, int offset){
        int place = placePlayer(playerId, offset);
        return pkPlayerStates.get(place);
    }

    /**
     * Modifie une information spécifique d'un joueur dans le tableau modifiable.
     *
     * @param pkPlayerStates le tableau des états empaquetés modifiable
     * @param playerId le joueur concerné
     * @param offset le décalage de l'information à modifier
     * @param newValue la nouvelle valeur empaquetée à assigner
     */
    private static void setPlayerInfoInArray(int[] pkPlayerStates, PlayerId playerId, int offset, int newValue) {
        int place = placePlayer(playerId, offset);
        pkPlayerStates[place] = newValue;
    }

    /**
     * Retourne le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés
     * @param playerId le joueur concerné
     * @return le contenu empaqueté des lignes de motif
     */
    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return getPlayerInfoInArray(pkPlayerStates, playerId, PATTERN_OFFSET);
    }

    /**
     * Retourne le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés
     * @param playerId le joueur concerné
     * @return le contenu empaqueté de la ligne plancher
     */
    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return getPlayerInfoInArray(pkPlayerStates, playerId, FLOOR_OFFSET);
    }

    /**
     * Retourne le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés
     * @param playerId le joueur concerné
     * @return le contenu empaqueté du mur
     */
    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return getPlayerInfoInArray(pkPlayerStates, playerId, WALL_OFFSET);
    }

    /**
     * Retourne le nombre de points du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés
     * @param playerId le joueur concerné
     * @return le nombre de points du joueur
     */
    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return getPlayerInfoInArray(pkPlayerStates, playerId, POINTS_OFFSET);
    }

    /**
     * Change le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés (modifiable)
     * @param playerId le joueur concerné
     * @param pkPatterns le nouveau contenu empaqueté des lignes de motif
     */
    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns){
        setPlayerInfoInArray(pkPlayerStates, playerId, PATTERN_OFFSET, pkPatterns);
    }

    /**
     * Change le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés (modifiable)
     * @param playerId le joueur concerné
     * @param pkFloor le nouveau contenu empaqueté de la ligne plancher
     */
    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor){
        setPlayerInfoInArray(pkPlayerStates, playerId, FLOOR_OFFSET, pkFloor);
    }

    /**
     * Change le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés (modifiable)
     * @param playerId le joueur concerné
     * @param pkWall le nouveau contenu empaqueté du mur
     */
    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall){
        setPlayerInfoInArray(pkPlayerStates, playerId, WALL_OFFSET, pkWall);

    }

    /**
     * Ajoute un nombre de points donné (pouvant être négatif) au joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés (modifiable)
     * @param playerId le joueur concerné
     * @param pointsToAdd le nombre de points à ajouter
     */
    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd){
        int place = placePlayer(playerId, POINTS_OFFSET);
        pkPlayerStates[place] = Math.max(0, pkPlayerStates[place] + pointsToAdd);
    }



}
