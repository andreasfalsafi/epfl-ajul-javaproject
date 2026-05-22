package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/**
 * Représente un joueur dans une partie d'Ajul.
 *
 * <p>Cette interface est annotée avec {@link FunctionalInterface}, permettant
 * de définir des joueurs via des expressions lambda ou des références de méthodes.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
@FunctionalInterface
public interface Player {

    /**
     * Détermine le prochain coup à jouer pour ce joueur.
     *
     * <p>On fait l'hypothèse que le joueur courant de l'état de jeu passé en
     * argument est le joueur auquel on applique cette méthode.
     *
     * @param gameState l'état actuel de la partie (en lecture seule)
     * @return le coup choisi par le joueur, représenté par un {@code short} empaqueté
     */
    short nextMove(ReadOnlyGameState gameState);
}
