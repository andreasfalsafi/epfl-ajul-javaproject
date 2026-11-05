package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/**
 * Représente un emplacement logique qu'une tuile peut occuper sur le plateau de jeu
 * ou hors de celui-ci (sac, boîte).
 * * <p>Cette interface est scellée et regroupe l'ensemble des types d'emplacements
 * possibles via des enregistrements imbriqués.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public sealed interface TileLocation {

    /**
     * Emplacement hors plateau (sac ou couvercle de la boîte).
     *
     * @param tileKind la sorte de tuile
     * @param index    l'index dans le conteneur
     */
    record OffBoard(TileKind tileKind, int index) implements TileLocation {
    }

    /**
     * Emplacement sur une source de tuiles (fabrique ou zone centrale).
     *
     * @param source la source concernée
     * @param index  l'index de l'emplacement dans la source
     */
    record OnSource(TileSource source, int index) implements TileLocation {
    }

    /**
     * Emplacement sur une ligne de motif d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param line     la ligne de motif concernée
     * @param index    l'index sur la ligne
     */
    record OnPattern(PlayerId playerId, TileDestination.Pattern line, int index) implements TileLocation {
    }

    /**
     * Emplacement sur le mur d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param line     la ligne du mur (correspondant à la ligne de motif)
     * @param color    la couleur de la tuile occupant l'emplacement
     */
    record OnWall(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color) implements TileLocation {
    }

    /**
     * Emplacement sur la ligne plancher d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param index    l'index sur la ligne (entre 0 et 6)
     */
    record OnFloor(PlayerId playerId, int index) implements TileLocation {
    }


}
