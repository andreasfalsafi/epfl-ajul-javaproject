package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

/**
 * Classe utilitaire permettant de manipuler des ensembles de tuiles (multisets)
 * empaquetés dans des entiers de type {@code int} (32 bits).
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class PkTileSet {

    /**
     * Représente l'ensemble de tuiles vide.
     */
    public static final int EMPTY = 0;

    /**
     * Représente l'ensemble de tuiles plein (20 de chaque couleur + 1 marqueur).
     */
    public static final int FULL = 0b01_010100_010100_010100_010100_010100;

    /**
     * Représente l'ensemble de tuiles colorées plein (sans le marqueur).
     */
    public static final int FULL_COLORED = 0b00_010100_010100_010100_010100_010100;

    private static final int MASK = 0b111111;

    private static final int BITS_PER_COLOR = 6;
    private static final int SHIFT_2_COLORS = 2*BITS_PER_COLOR;
    private static final int SHIFT_4_COLORS = 4*BITS_PER_COLOR;

    /**
     * Outil de débogage
     * Vérifie si un ensemble de tuiles empaqueté est valide.
     * @param pkTileSet l'ensemble de tuiles empaqueté
     *
     * @return true si l'ensemble est valide, false sinon
     */
    private static boolean isValid(int pkTileSet) {
        // Le bit 31 doit être à 0 (le nombre doit être positif)
        if (pkTileSet < 0) return false;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int countOf = (pkTileSet >>> color.index() * BITS_PER_COLOR) & MASK;
            if (countOf > color.tilesCount()) {
                return false;
            }
        }
        int numFPM = (pkTileSet >>> TileKind.FIRST_PLAYER_MARKER.index() * BITS_PER_COLOR) & MASK;
        return numFPM == 0 || numFPM == 1;
    }



    /**
     * Retourne un ensemble ne contenant que le nombre donné de tuiles de la sorte donnée.
     *
     * @param count    le nombre de tuiles
     * @param tileKind la sorte de tuile
     * @return l'ensemble de tuiles empaqueté
     */
    public static int of(int count, TileKind tileKind) {
        boolean isValid = count >= 0 && count <= tileKind.tilesCount();
        assert isValid;

        return count << tileKind.index() * BITS_PER_COLOR;
    }


    /**
     * Indique si l'ensemble de tuiles est vide.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté
     * @return true s'il est vide, false sinon
     */
    public static boolean isEmpty(int pkTileSet) {
        assert isValid(pkTileSet);

        return pkTileSet == EMPTY;
    }

    /**
     * Calcule la taille totale de l'ensemble (le nombre total de tuiles qu'il contient)
     * en utilisant une technique SWAR.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté
     * @return le nombre total de tuiles
     */
    public static int size(int pkTileSet) {
        assert isValid(pkTileSet);

        int s = pkTileSet + (pkTileSet >>> BITS_PER_COLOR);
        return (s & MASK) + ((s >>> SHIFT_2_COLORS) & MASK) + ((s >>> (SHIFT_4_COLORS)) & MASK);
    }

    /**
     * Retourne le nombre de tuiles d'une sorte donnée dans l'ensemble.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté
     * @param tilekind  la sorte de tuile
     * @return le nombre de tuiles de cette sorte
     */
    public static int countOf(int pkTileSet, TileKind tilekind) {
        assert isValid(pkTileSet);

        return (pkTileSet >>> tilekind.index() * BITS_PER_COLOR) & MASK;
    }

    /**
     * Retourne le sous-ensemble constitué uniquement des tuiles de la sorte donnée.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté
     * @param tilekind  la sorte de tuile à conserver
     * @return le sous-ensemble empaqueté
     */
    public static int subsetOf(int pkTileSet, TileKind tilekind) {
        assert isValid(pkTileSet);

        return pkTileSet & (MASK << tilekind.index() * BITS_PER_COLOR);
    }

    /**
     * Ajoute exactement une tuile de la sorte donnée à l'ensemble.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté initial
     * @param tilekind  la sorte de tuile à ajouter
     * @return le nouvel ensemble de tuiles empaqueté
     */
    public static int add(int pkTileSet, TileKind tilekind) {
        assert isValid(pkTileSet);

        return pkTileSet + (1 << tilekind.index() * BITS_PER_COLOR);
    }

    /**
     * Retire exactement une tuile de la sorte donnée de l'ensemble.
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté initial
     * @param tilekind  la sorte de tuile à retirer
     * @return le nouvel ensemble de tuiles empaqueté
     */
    public static int remove(int pkTileSet, TileKind tilekind) {
        assert isValid(pkTileSet);

        return pkTileSet - (1 << tilekind.index() * BITS_PER_COLOR);
    }

    /**
     * Calcule l'union de deux ensembles de tuiles.
     *
     * @param pkTileSet1 le premier ensemble empaqueté
     * @param pkTileSet2 le deuxième ensemble empaqueté
     * @return l'union des deux ensembles empaquetée
     */
    public static int union(int pkTileSet1, int pkTileSet2) {
        assert isValid(pkTileSet1) && isValid(pkTileSet2);

        return pkTileSet1 + pkTileSet2;
    }

    /**
     * Calcule la différence entre un ensemble et un de ses sous-ensembles.
     *
     * @param pkTileSet1 l'ensemble parent empaqueté
     * @param pkTileSet2 le sous-ensemble empaqueté à soustraire
     * @return la différence des deux ensembles empaquetée
     */
    public static int difference(int pkTileSet1, int pkTileSet2) {
        assert isValid(pkTileSet1) && isValid(pkTileSet2);

        return pkTileSet1 - pkTileSet2;
    }

    /**
     * Copie les tuiles colorées de l'ensemble dans un tableau, triées par couleur.
     *
     * @param pkTileSet   l'ensemble de tuiles empaqueté
     * @param destination le tableau dans lequel copier les tuiles
     * @return l'index de l'élément qui suit le dernier écrit
     */
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        assert isValid(pkTileSet);

        int from = 0;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int numberOfSaidColor = countOf(pkTileSet, color);
            int to = from + numberOfSaidColor;
            Arrays.fill(destination, from, to, color);
            from = to;
        }
        return from;
    }

    /**
     * Extrait un échantillon aléatoire de tuiles colorées en utilisant l'échantillonnage par réservoir.
     *
     * @param pkTileSet       l'ensemble de tuiles empaqueté source
     * @param destination     le tableau servant de réservoir
     * @param offset          l'index de départ dans le tableau de destination
     * @param randomGenerator le générateur de nombres aléatoires à utiliser
     * @return l'index de l'élément qui suit le dernier écrit dans la destination
     */
    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator) {
        assert isValid(pkTileSet);

        int sizeReservoir = destination.length - offset;
        int i = 0;

        for (TileKind.Colored color : TileKind.Colored.ALL){
            int numTiles = countOf(pkTileSet, color);

            for (int c = 0; c < numTiles; c++){
                if (i < sizeReservoir){

                    destination[offset + i] = color;
                } else {
                    int j = randomGenerator.nextInt(i + 1);

                    if (j < sizeReservoir) {
                        destination[offset + j] = color;
                    }
                }

                i++;
            }
        }
        return offset + i;
    }

    /**
     * Retourne la représentation textuelle de l'ensemble de tuiles.
     * Le format est du type : {20*A,20*B,1*FIRST_PLAYER_MARKER}
     *
     * @param pkTileSet l'ensemble de tuiles empaqueté
     * @return la chaîne de caractères représentant l'ensemble
     */
    public static String toString(int pkTileSet) {
        assert isValid(pkTileSet);

        StringJoiner j = new StringJoiner(",", "{", "}");

        for (TileKind.Colored coloredKind : TileKind.Colored.ALL) {
            int count = countOf(pkTileSet, coloredKind);

            if (count > 0) {
                j.add(count + "*" + coloredKind.name());
            }
        }
        int numFPM = countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        if (numFPM > 0) {
            j.add(numFPM + "*" + TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER.name());
        }

        return j.toString();
    }
}