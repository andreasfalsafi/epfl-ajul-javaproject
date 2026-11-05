package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Contient des méthodes statiques permettant de manipuler le contenu des lignes de motif d'un joueur,
 * représenté de manière empaquetée.
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class PkPatterns {

    /**
     * Représente les lignes de motif vides.
     */
    public static final int EMPTY = 0;

    // --- Constantes structurelles pour une ligne ---
    private static final int COUNT_BITS = 3;
    private static final int COUNT_OFFSET = 0;
    private static final int COUNT_MASK = (1 << COUNT_BITS) - 1;

    private static final int COLOR_BITS = 3;
    private static final int COLOR_OFFSET = COUNT_OFFSET + COUNT_BITS;
    private static final int COLOR_MASK = ((1 << COLOR_BITS) - 1) << COLOR_OFFSET;

    private static final int LINE_BITS = COUNT_BITS + COLOR_BITS; // 6 bits par ligne

    /**
     * Retourne le nombre de tuiles présentes sur la ligne de motif donnée des lignes de motif empaquetées.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif ciblée
     * @return le nombre de tuiles présentes sur la ligne
     */
    public static int size(int pkPatterns, TileDestination.Pattern line){
        assert isValid(pkPatterns);
        int lineShift = line.index() * LINE_BITS;
        return (pkPatterns >>> (lineShift + COUNT_OFFSET)) & COUNT_MASK;    }

    /**
     * Retourne la couleur des tuiles présentes sur la ligne de motif donnée des lignes de motif empaquetées.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif ciblée
     * @return la couleur des tuiles présentes sur la ligne
     */
    public static TileKind.Colored color(int pkPatterns, TileDestination.Pattern line) {
        assert isValid(pkPatterns);
        assert size(pkPatterns, line) > 0;

        int lineShift = line.index() * LINE_BITS;
        int colorIndex = (pkPatterns >>> (lineShift + COLOR_OFFSET)) & COUNT_MASK;
        return TileKind.Colored.ALL.get(colorIndex);
    }


    /**
     * Retourne vrai si et seulement si la ligne de motif donnée des lignes de motif empaquetées est pleine.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif à vérifier
     * @return true si la ligne contient le nombre maximum de tuiles qu'elle peut contenir, false sinon
     */
    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        assert isValid(pkPatterns);
        return size(pkPatterns, line) == line.capacity();
    }

    /**
     * Retourne vrai si et seulement si la ligne de motif donnée des lignes de motif empaquetées
     * peut contenir des tuiles de la couleur spécifiée.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif à vérifier
     * @param color la couleur des tuiles à vérifier
     * @return true si la ligne est vide ou si elle contient déjà des tuiles de cette couleur, false sinon
     */
    public static boolean canContain(int pkPatterns, TileDestination.Pattern line, TileKind.Colored color) {
        assert isValid(pkPatterns);
        if (size(pkPatterns, line) == 0) return true;
        return color(pkPatterns, line) == color;
    }


    /**
     * Retourne des lignes de motif empaquetées identiques à celles données,
     * mais avec le nombre spécifié de tuiles de la couleur donnée ajoutées à la ligne.
     *
     * @param pkPatterns les lignes de motif empaquetées initiales
     * @param line la ligne de motif où ajouter les tuiles
     * @param tileCount le nombre de tuiles à ajouter
     * @param color la couleur des tuiles ajoutées
     * @return les nouvelles lignes de motif empaquetées
     */
    public static int withAddedTiles(int pkPatterns, TileDestination.Pattern line, int tileCount, TileKind.Colored color) {
        assert isValid(pkPatterns);
        assert canContain(pkPatterns, line, color);
        assert size(pkPatterns, line) + tileCount <= line.capacity();

        if (tileCount == 0) return pkPatterns;

        int lineShift = line.index() * LINE_BITS;

        if (size(pkPatterns, line) > 0) {
            return pkPatterns + (tileCount << (lineShift + COUNT_OFFSET));
        } else {
            int newColorBits = color.index() << COLOR_OFFSET;
            int newCountBits = tileCount << COUNT_OFFSET;
            return pkPatterns | ((newColorBits | newCountBits) << lineShift);
        }
    }

    /**
     * Retourne des lignes de motif empaquetées identiques à celles données,
     * mais avec la ligne spécifiée rendue vide.
     *
     * @param pkPatterns les lignes de motif empaquetées initiales
     * @param line la ligne de motif à vider
     * @return les nouvelles lignes de motif empaquetées
     */
    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {
        assert isValid(pkPatterns);
        int lineShift = line.index() * LINE_BITS;
        int lineMask = (COUNT_MASK | COLOR_MASK) << lineShift;
        return pkPatterns & ~lineMask;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles
     * se trouvant sur les lignes de motif empaquetées.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkPatterns){
        assert isValid(pkPatterns);

        int rendu =0;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL){
            int numTiles = size(pkPatterns, line);
            if (numTiles > 0) {
                TileKind.Colored colorTile = color(pkPatterns, line);
                rendu = PkTileSet.union(rendu, PkTileSet.of(numTiles, colorTile));
            }
        }
        return rendu;
    }

    /**
     * Retourne la représentation textuelle des lignes de motif empaquetées.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return la représentation textuelle sous forme de chaîne de caractères
     */
    public static String toString(int pkPatterns) {
        assert isValid(pkPatterns);


        StringJoiner j = new StringJoiner(", ", "[","]");

        for(TileDestination.Pattern line : TileDestination.Pattern.ALL){
            int numTiles = size(pkPatterns, line);

            String tiles = numTiles > 0 ? color(pkPatterns, line).name().repeat(numTiles) : "";
            String dots = ".".repeat(line.capacity() - numTiles);

            j.add(tiles + dots);
        }
        return j.toString();
    }

    private static boolean isValid(int pkPatterns) {
        if ((pkPatterns >>> 30) != 0) return false;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int lineShift = line.index() * LINE_BITS;
            int assertSize = (pkPatterns >>> (lineShift + COUNT_OFFSET)) & COUNT_MASK;
            int assertColor = (pkPatterns >>> (lineShift + COLOR_OFFSET)) & (COUNT_MASK);

            if (assertSize > line.capacity()) return false;
            if (assertColor >= TileKind.Colored.ALL.size()) return false;
            if (assertSize == 0 && assertColor != 0) return false;
        }
        return true;
    }

}
