package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Contient des méthodes statiques permettant de manipuler le contenu d'une ligne plancher
 * empaquetée dans un entier de type int.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class PkFloor {

    /**
     * Représente la ligne plancher vide.
     */
    public static final int EMPTY = 0;

    // une information individuelle de Floor (la Size comme les tuiles individuelles sont représentées par 3 bits).
    private static final int MASK = 0b111;

    private static final int BITS_PER_TILE = 3;

    private static final int FLOOR_SIZE_SHIFT = 1;

    private static boolean isValid(int pkFloor){
        int sizePkFloor = pkFloor & MASK;
        //les 8 bits de poid fort valent toujours 0
        if (pkFloor >>> 24 != 0){
            return false;
        }
        for (int i = 0; i < TileDestination.FLOOR.capacity(); i++){
            int indexOfColor = (pkFloor >>> ((i + FLOOR_SIZE_SHIFT) * BITS_PER_TILE)) & MASK;
            boolean indexOk = indexOfColor <= TileKind.COUNT; //l'index de FPM est le plus grand possible

            if (!indexOk){
                return false;
            }
            if (i >= sizePkFloor && indexOfColor != 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne la taille de la ligne plancher empaquetée, c'est-à-dire le nombre de tuiles qu'elle contient.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return la taille de la ligne plancher (entre 0 et 7)
     */
    public static int size(int pkFloor) {
        assert isValid(pkFloor);

        return pkFloor & MASK;
    }


    /**
     * Retourne la sorte de tuile se trouvant à l'index donné de la ligne plancher empaquetée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @param i l'index de la tuile (doit être strictement inférieur à la taille de la ligne)
     * @return la sorte de tuile à cet index
     */
    public static TileKind tileAt(int pkFloor, int i) {
        assert isValid(pkFloor);
        assert i >= 0 && i < size(pkFloor);

        return TileKind.ALL.get(MASK & (pkFloor >>> (BITS_PER_TILE * (i + FLOOR_SIZE_SHIFT))));
    }


    /**
     * Retourne une ligne plancher empaquetée identique à celle donnée, mais avec les tuiles
     * de l'ensemble empaqueté ajoutées par ordre de sorte (A, B, C, D, E, marqueur).
     * Si la ligne n'a pas la capacité d'accueillir toutes ces tuiles, les excédentaires sont ignorées
     * sauf le marqueur de premier joueur qui doit toujours être ajouté (remplaçant la dernière tuile si besoin).
     *
     * @param pkFloor la ligne plancher empaquetée initiale
     * @param pkTileSet l'ensemble de tuiles empaqueté à ajouter
     * @return la nouvelle ligne plancher empaquetée
     */
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        assert isValid(pkFloor);

        int currentSize = size(pkFloor);

        for (TileKind e : TileKind.ALL) {
            int numOfTiles = PkTileSet.countOf(pkTileSet, e);

            for (int u = 0; u < numOfTiles; u++) {
                if (currentSize < TileDestination.FLOOR.capacity()) {
                    pkFloor = pkFloor | (e.index() << (currentSize + FLOOR_SIZE_SHIFT) * BITS_PER_TILE);
                    currentSize++;
                }
                else if (e == TileKind.FIRST_PLAYER_MARKER){
                    int offset = TileDestination.FLOOR.capacity() * BITS_PER_TILE;
                    pkFloor = (pkFloor & ~(MASK << offset)) | (e.index() << offset);
                }
            }
        }
        pkFloor = (pkFloor & ~MASK) | currentSize;

        return pkFloor;
    }

    /**
     * Retourne vrai si et seulement si la ligne plancher empaquetée contient le marqueur de premier joueur.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return true si le marqueur est présent, false sinon
     */
    public static boolean containsFirstPlayerMarker(int pkFloor){
        assert isValid(pkFloor);

        for (int i = 0; i < size(pkFloor); i++){
            if (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER){
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles se trouvant
     * sur la ligne plancher empaquetée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkFloor){
        assert isValid(pkFloor);

        int result = PkTileSet.EMPTY;
        for(int i = 0; i < size(pkFloor); i++) {
            result = PkTileSet.add(result, tileAt(pkFloor, i));
        }
        return result;
    }

    /**
     * Retourne la représentation textuelle de la ligne plancher empaquetée.
     * Le format est une liste des noms des sortes de tuiles entourée de crochets, par exemple : [B, B, FIRST_PLAYER_MARKER].
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return la chaîne de caractères représentant la ligne plancher
     */
    public static String toString(int pkFloor){
        assert isValid(pkFloor);

        StringJoiner j = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size(pkFloor); i++){
            j.add(tileAt(pkFloor, i).toString());
        }
        return j.toString();
    }
}