package ch.epfl.ajul;

import java.util.List;

/**
 * Identifie les joueurs d'une partie d'Ajul.
 * <p>
 * Ce type énuméré contient les identifiants pour les quatre joueurs possibles
 * ainsi qu'une liste immuable les regroupant tous.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public enum PlayerId {
    /** Le premier joueur (Joueur 1). */
    P1,
    /** Le deuxième joueur (Joueur 2). */
    P2,
    /** Le troisième joueur (Joueur 3). */
    P3,
    /** Le quatrième joueur (Joueur 4). */
    P4;

    /**
     * Une liste immuable contenant tous les identifiants de joueurs,
     * dans l'ordre de leur déclaration (de P1 à P4).
     */
    public static final List<PlayerId> ALL = List.of(PlayerId.values());

}
