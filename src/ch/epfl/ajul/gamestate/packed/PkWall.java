package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Contient des méthodes statiques permettant de manipuler le mur d'un joueur,
 * représenté de manière empaquetée dans un entier de type int (25 bits utilisés).
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class PkWall {

    /**
     * Représente le mur vide (aucun bit n'est à 1).
     */
    public static final int EMPTY = PkIntSet32.EMPTY;

    /**
     * La largeur du mur (nombre de colonnes).
     */
    public static final int WALL_WIDTH = TileKind.Colored.COUNT;

    /**
     * La hauteur du mur (nombre de lignes).
     */
    public static final int WALL_HEIGHT = TileDestination.Pattern.COUNT;

    // Masque pour une ligne pleine (5 bits à 1)
    private static final int FULL_ROW_MASK = 0b11111;

    // Masque pour une colonne pleine (bits 0, 5, 10, 15, 20)
    private static final int FULL_COLUMN_MASK = 0b00001_00001_00001_00001_00001;

    /*
    private static final int A_COLOR_MASK = 0b0000000_10000_01000_00100_00010_00001;
    private static final int B_COLOR_MASK = 0b0000000_00001_10000_01000_00100_00010;
    private static final int C_COLOR_MASK = 0b0000000_00010_00001_10000_01000_00100;
    private static final int D_COLOR_MASK = 0b0000000_00100_00010_00001_10000_01000;
    private static final int E_COLOR_MASK = 0b0000000_01000_00100_00010_00001_10000;

    private static final int[] COLOR_MASKS = {
            A_COLOR_MASK,
            B_COLOR_MASK,
            C_COLOR_MASK,
            D_COLOR_MASK,
            E_COLOR_MASK
    };

     */

    private static boolean isValid(int pkWall){
        return pkWall >>> 25 == 0;
    }

    /**
     * Retourne l'index (de 0 à 24) de la case du mur correspondant à la ligne et la couleur données.
     *
     * @param line la ligne du mur
     * @param color la couleur de la tuile
     * @return l'index de la case dans la représentation empaquetée
     */
    public static int indexOf(TileDestination.Pattern line, TileKind.Colored color){
        return (WALL_WIDTH * line.index()) + column(line, color);
    }

    /**
     * Retourne l'index de la colonne (de 0 à 4) correspondant à la ligne et la couleur données.
     *
     * @param line la ligne du mur
     * @param color la couleur de la tuile
     * @return l'index de la colonne
     */
    public static int column(TileDestination.Pattern line, TileKind.Colored color){
        return (line.index() + color.index()) % WALL_WIDTH;
    }

    /**
     * Retourne la couleur de la case se trouvant à la ligne et la colonne données.
     *
     * @param line la ligne du mur
     * @param column l'index de la colonne
     * @return la couleur de la case correspondante
     */
    public static TileKind.Colored colorAt(TileDestination.Pattern line, int column){
        return TileKind.Colored.ALL.get((column - line.index() + WALL_WIDTH) % WALL_WIDTH);
    }

    /**
     * Retourne le mur empaqueté identique à celui donné, mais dans lequel la case
     * correspondant à la ligne et la couleur données contient une tuile.
     *
     * @param pkWall le mur empaqueté initial
     * @param line la ligne où ajouter la tuile
     * @param color la couleur de la tuile à ajouter
     * @return le nouveau mur empaqueté
     */
    public static int withTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color){
        assert isValid(pkWall);

        int ref = indexOf(line, color);
        return PkIntSet32.add(pkWall, ref);
    }

    /**
     * Retourne vrai si et seulement si la case du mur correspondant à la ligne
     * et la couleur données contient une tuile.
     *
     * @param pkWall le mur empaqueté
     * @param line la ligne à vérifier
     * @param color la couleur de la tuile à vérifier
     * @return true si la case contient une tuile, false sinon
     */
    public static boolean hasTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color){
        assert isValid(pkWall);

        return PkIntSet32.contains(pkWall, indexOf(line, color));
    }

    /**
     * Extrait les 5 bits correspondant à une colonne spécifique du mur.
     *
     * @param pkWall le mur empaqueté
     * @param column l'index de la colonne à extraire
     * @return un entier dont les 5 bits de poids faible représentent la colonne
     */
    private static int columnBits(int pkWall, int column){
        assert isValid(pkWall);

        int result = 0;
        for(int i = 0; i < WALL_HEIGHT; i++){
            result |= (((pkWall >>> ((WALL_HEIGHT * i) + column)) & 1) << i);
        }
        return result;
    }

    /**
     * Retourne la taille du groupe horizontal contenant la case spécifiée.
     *
     * @param pkWall le mur empaqueté
     * @param line la ligne de la case
     * @param color la couleur de la case
     * @return la taille du groupe horizontal (0 si la case est vide)
     */
    public static int hGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color){
        assert isValid(pkWall);
        int rowBits = (pkWall >>> (WALL_WIDTH * line.index())) & FULL_ROW_MASK;

        return adjacentBits(rowBits, column(line, color), WALL_WIDTH);
    }

    /**
     * Retourne la taille du groupe vertical contenant la case spécifiée.
     *
     * @param pkWall le mur empaqueté
     * @param line la ligne de la case
     * @param color la couleur de la case
     * @return la taille du groupe vertical (0 si la case est vide)
     */
    public static int vGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color){
        assert isValid(pkWall);

        return adjacentBits(columnBits(pkWall, column(line, color)), line.index(), WALL_HEIGHT);
    }

    /**
     * Retourne vrai si et seulement si le mur possède au moins une ligne complètement pleine.
     *
     * @param pkWall le mur empaqueté
     * @return true si au moins une ligne est pleine, false sinon
     */
    public static boolean hasFullRow(int pkWall){
        assert isValid(pkWall);

        for(TileDestination.Pattern line : TileDestination.Pattern.ALL){
            if(isRowFull(pkWall, line)) return true;
        }

        return false;
    }

    /**
     * Retourne vrai si la ligne spécifiée du mur est complètement pleine.
     * Utilise {@link PkIntSet32#containsAll(int, int)}.
     *
     * @param pkWall le mur empaqueté
     * @param line   la ligne à vérifier
     * @return true si la ligne est pleine, false sinon
     */
    public static boolean isRowFull(int pkWall, TileDestination.Pattern line) {
        assert isValid(pkWall);
        int rowMask = FULL_ROW_MASK << (WALL_WIDTH * line.index());
        return PkIntSet32.containsAll(pkWall, rowMask);
    }

    /**
     * Retourne vrai si la colonne spécifiée du mur est complètement pleine.
     * Utilise {@link PkIntSet32#containsAll(int, int)}.
     *
     * @param pkWall le mur empaqueté
     * @param column l'index de la colonne à vérifier
     * @return true si la colonne est pleine, false sinon
     */
    public static boolean isColumnFull(int pkWall, int column) {
        assert isValid(pkWall);
        assert column >= 0 && column < WALL_WIDTH;
        int colMask = FULL_COLUMN_MASK << column;
        return PkIntSet32.containsAll(pkWall, colMask);
    }

    /**
     * Retourne vrai si toutes les cases de la couleur spécifiée sont pleines.
     * Utilise {@link PkIntSet32#containsAll(int, int)}.
     *
     * @param pkWall le mur empaqueté
     * @param color  la couleur à vérifier
     * @return true si les 5 tuiles de cette couleur sont sur le mur, false sinon
     */
    public static boolean isColorFull(int pkWall, TileKind.Colored color) {
        assert isValid(pkWall);
        return PkIntSet32.containsAll(pkWall, colorMask(color));
    }


    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles se trouvant sur le mur.
     *
     * @param pkWall le mur empaqueté
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkWall) {
        assert isValid(pkWall);
        int pkTileSetResult = PkTileSet.EMPTY;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int numberOfColor = Integer.bitCount(pkWall & colorMask(color));
            pkTileSetResult = PkTileSet.union(pkTileSetResult, PkTileSet.of(numberOfColor, color));
        }
        return pkTileSetResult;
    }

    /**
     * Retourne la représentation textuelle du mur.
     * Les cases pleines sont représentées par leur couleur en majuscule,
     * les cases vides en minuscule.
     *
     * @param pkWall le mur empaqueté
     * @return la représentation textuelle du mur
     */
    public static String toString(int pkWall){
        assert isValid(pkWall);

        StringJoiner j = new StringJoiner(", ", "[", "]");

        for (int i = 0; i < WALL_HEIGHT; i++){
            StringBuilder b = new StringBuilder(WALL_WIDTH);
            TileDestination.Pattern line = TileDestination.Pattern.ALL.get(i);

            for (int col = 0; col < WALL_WIDTH; col++) {
                String colorName = colorAt(line, col).name();
                int bitIndex = (i * WALL_WIDTH) + col;

                b.append(PkIntSet32.contains(pkWall, bitIndex) ? colorName : colorName.toLowerCase());

            }
            j.add(b);
        }
        return j.toString();
    }

    /**
     * Retourne le masque correspondant aux 5 cases d'une couleur donnée dans le mur.
     */
    private static int colorMask(TileKind.Colored color) {
        return switch (color) {
            case A -> 0b0000000_10000_01000_00100_00010_00001;
            case B -> 0b0000000_00001_10000_01000_00100_00010;
            case C -> 0b0000000_00010_00001_10000_01000_00100;
            case D -> 0b0000000_00100_00010_00001_10000_01000;
            case E -> 0b0000000_01000_00100_00010_00001_10000;
        };
    }
    /**
     * Calcule la taille d'un groupe de bits adjacents valant 1 autour d'une position donnée.
     * La logique de comptage directionnel a été extraite pour éviter la duplication.
     */
    private static int adjacentBits(int bitSequence, int bitPosition, int maxLength) {
        if ((bitSequence & (1 << bitPosition)) == 0) return 0;

        // Taille initiale à 1 (inclut le bit à la position actuelle)
        return 1 + countAdjacentDirectional(bitSequence, bitPosition, -1, 0)
                + countAdjacentDirectional(bitSequence, bitPosition, 1, maxLength - 1);
    }

    /**
     * Compte le nombre de bits consécutifs à 1 dans une direction donnée (-1 ou +1).
     */
    private static int countAdjacentDirectional(int bitSequence, int startPos, int step, int bound) {
        int count = 0;
        for (int i = startPos + step; (step > 0 ? i <= bound : i >= bound); i += step) {
            if ((bitSequence & (1 << i)) != 0) count++;
            else break;
        }
        return count;
    }

}
