package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/**
 * Classe utilitaire permettant de manipuler des coups (moves) empaquetés
 * dans des entiers de type {@code short} (16 bits).
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class PkMove {

    // --- Définition des masques et décalages ---
    private static final int SOURCE_OFFSET = 0;
    private static final int SOURCE_BITS = 4;
    private static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;

    private static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;

    private static final int COLOR_BITS = 3;
    private static final int COLOR_MASK = ((1 << COLOR_BITS) - 1) << COLOR_OFFSET;

    private static final int DESTINATION_OFFSET = COLOR_OFFSET + COLOR_BITS;

    private static final int DESTINATION_BITS = 3;
    private static final int DESTINATION_MASK = ((1 << DESTINATION_BITS) - 1) << DESTINATION_OFFSET;



    private static boolean isValid(short pkMove){
        // creation des variables
        int source = pkMove & SOURCE_MASK;
        int color = (pkMove & COLOR_MASK) >>> COLOR_OFFSET;
        int destination = (pkMove & DESTINATION_MASK) >>> DESTINATION_OFFSET;

        // verification des bornes
        if (source >= TileSource.COUNT) return false;      // 10 sources max (0 à 9)
        if (color >= TileKind.Colored.COUNT) return false;       // 5 couleurs max (0 à 4)
        if (destination >= TileDestination.COUNT) return false; // 6 destinations max (0 à 5)

        // verification des 6 bits de poids fort (ils doivent etre à 0)
        // Short.toUnsignedInt evite les problèmes de signe si le premier bit est 1
        return (Short.toUnsignedInt(pkMove) >>> 10) == 0;
    }

    /**
     * Empaquète les informations d'un coup dans un entier de type {@code short}.
     *
     * @param source      la source de laquelle les tuiles sont prises
     * @param color       la couleur des tuiles choisies
     * @param destination la destination sur laquelle les tuiles sont placées
     * @return le coup empaqueté sous forme de {@code short}
     */
    public static short pack(TileSource source, TileKind.Colored color, TileDestination destination){
        int sourceByte = source.index();
        int colorByte = color.index() << COLOR_OFFSET;
        int destinationByte = destination.index() << DESTINATION_OFFSET;

        short pkMove = (short) (sourceByte | colorByte | destinationByte);
        assert isValid(pkMove);
        return pkMove;
    }

    /**
     * Extrait la source d'un coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la source ({@link TileSource}) correspondante
     */
    public static TileSource source(short pkMove){
        assert isValid(pkMove);
        int sourceIndex = pkMove & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }

    /**
     * Extrait la couleur des tuiles d'un coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la couleur ({@link TileKind.Colored}) correspondante
     */
    public static TileKind.Colored color(short pkMove) {
        assert isValid(pkMove);
        int colorIndex = (pkMove & COLOR_MASK) >>> COLOR_OFFSET;
        return TileKind.Colored.ALL.get(colorIndex);
    }

    /**
     * Extrait la destination d'un coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la destination ({@link TileDestination}) correspondante
     */
    public static TileDestination destination(short pkMove){
        assert isValid(pkMove);
        int destIndex = (pkMove & DESTINATION_MASK) >>> DESTINATION_OFFSET;
        return TileDestination.ALL.get(destIndex);
    }


}
