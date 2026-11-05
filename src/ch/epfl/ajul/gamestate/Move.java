package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

/**
 * Représente un coup (Move) dans le jeu Ajul.
 * <p>
 * Un coup est défini par une source (d'où l'on prend les tuiles), une couleur de tuile,
 * et une destination (où l'on pose les tuiles).
 *
 * @param source      la source des tuiles (fabrique ou centre)
 * @param tileColor   la couleur des tuiles prises
 * @param destination la destination sur le plateau du joueur
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public record Move(TileSource source, TileKind.Colored tileColor, TileDestination destination) {


    /**
     * Constructeur compact validant que les paramètres ne sont pas nuls.
     *
     * @throws NullPointerException si l'un des arguments est null
     */
    public Move{
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    /**
     * Crée un coup à partir de sa représentation empaquetée.
     *
     * @param pkMove le coup empaqueté (short)
     * @return le coup correspondant (objet Move)
     */
    public static Move ofPacked(short pkMove) {
        return new Move(PkMove.source(pkMove), PkMove.color(pkMove),PkMove.destination(pkMove));
    }

    /**
     * Retourne la représentation empaquetée de ce coup.
     *
     * @return le coup empaqueté (short)
     */
    public short packed(){
        return PkMove.pack(source, tileColor, destination);
    }

    /**
     * Nombre maximum de coups théoriquement possibles.
     * Calculé comme : (Nombre de fabriques) * (Tuiles par fabrique) * (Nombre de destinations).
     * Exemple (jeu à 4) : 9 * 4 * 6 = 216 coups.
     */
    public static final int MAX_MOVES = TileSource.Factory.COUNT * TileSource.Factory.TILES_PER_FACTORY * TileDestination.COUNT;
}
