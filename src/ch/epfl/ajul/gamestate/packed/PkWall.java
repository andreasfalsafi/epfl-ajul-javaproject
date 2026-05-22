package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Contient des méthodes statiques permettant de manipuler le mur d'un joueur,
 * représenté de manière empaquetée dans un entier de type int (25 bits utilisés).
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class PkWall {

    /**
     * Représente le mur vide (aucun bit n'est à 1).
     */
    public static final int EMPTY = 0;

    /**
     * La largeur du mur (nombre de colonnes).
     */
    public static final int WALL_WIDTH = 5;

    /**
     * La hauteur du mur (nombre de lignes).
     */
    public static final int WALL_HEIGHT = 5;

    // Masque pour extraire 5 bits (une ligne ou une colonne)
    private static final int MASK = 0b11111;

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
     * Calcule la taille d'un groupe de bits adjacents valant 1 autour d'une position donnée.
     *
     * @param bitSequence la séquence de bits à analyser
     * @param bitPosition la position de départ
     * @param bitSequenceLength la longueur totale de la séquence analysée
     * @return le nombre de bits adjacents valant 1 (incluant la position de départ)
     */
    private static int adjacentBits(int bitSequence, int bitPosition, int bitSequenceLength){
        int adjacentBitSize = 0;

        //si la case de départ est vide, le groupe est de taille 0
        if ((bitSequence & (1 << bitPosition)) == 0) {
            // on pourrait return adjacent bit size,mais je trouve que c'est plus lisible ainsi dans ce cas
            return 0;
        }
        //partie droite
        for(int i = bitPosition; i >= 0; i--){
            if((bitSequence & (1 << i)) != 0){
                adjacentBitSize++;
            }
            else {break;}
        }
        //partie gauche
        //bitposition+1 car on compte pas le bit en question
        for(int j = bitPosition + 1; j < bitSequenceLength; j++){
            if((bitSequence&(1 << j)) != 0){
                adjacentBitSize++;
            }
            else{break;}
        }
        return adjacentBitSize;
    }

    /**
     * Extrait les 5 bits correspondant à une ligne spécifique du mur.
     *
     * @param pkWall le mur empaqueté
     * @param line la ligne à extraire
     * @return un entier dont les 5 bits de poids faible représentent la ligne
     */
    private static int rowBits(int pkWall, TileDestination.Pattern line){
        assert isValid(pkWall);

        return (pkWall >>> (WALL_WIDTH * line.index())) & MASK;
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

        return adjacentBits(rowBits(pkWall, line),column(line, color), WALL_WIDTH);
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
     * Retourne vrai si et seulement si la ligne spécifiée du mur est complètement pleine.
     *
     * @param pkWall le mur empaqueté
     * @param line la ligne à vérifier
     * @return true si la ligne est pleine, false sinon
     */
    public static boolean isRowFull(int pkWall, TileDestination.Pattern line){
        //le MASK ici est plus utilisé en tant que "full"
        assert isValid(pkWall);

        return rowBits(pkWall, line) == MASK;
    }

    /**
     * Retourne vrai si et seulement si la colonne spécifiée du mur est complètement pleine.
     *
     * @param pkWall le mur empaqueté
     * @param column l'index de la colonne à vérifier
     * @return true si la colonne est pleine, false sinon
     */
    public static boolean isColumnFull(int pkWall,int column){
        assert isValid(pkWall);

        return columnBits(pkWall, column) == MASK;
    }


    /**
     * Retourne vrai si et seulement si toutes les cases de la couleur spécifiée sont pleines.
     *
     * @param pkWall le mur empaqueté
     * @param color la couleur à vérifier
     * @return true si les 5 tuiles de cette couleur sont sur le mur, false sinon
     */
    public static boolean isColorFull(int pkWall, TileKind.Colored color){
        assert isValid(pkWall);

        return (pkWall & COLOR_MASKS[color.index()]) == COLOR_MASKS[color.index()];
    }


    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles
     * se trouvant sur le mur.
     *
     * @param pkWall le mur empaqueté
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkWall) {
        assert isValid(pkWall);

        int pkTileSetResult = 0;
        for(TileKind.Colored color : TileKind.Colored.ALL){
            int numberOfSaidColorInWall = Integer.bitCount(pkWall&COLOR_MASKS[color.index()]);
            int thisColorPkTileSet = PkTileSet.of(numberOfSaidColorInWall, color);

            pkTileSetResult = PkTileSet.union(pkTileSetResult, thisColorPkTileSet);
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

}
