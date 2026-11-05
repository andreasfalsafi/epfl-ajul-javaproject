package ch.epfl.ajul;

/**
 * Classe utilitaire contenant des méthodes permettant de calculer
 * les points obtenus lors du jeu.
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class Points {

    /**
     * Bonus de points accordé pour une ligne du mur complètement pleine.
     */
    public static final int FULL_ROW_BONUS_POINTS = 2;

    /**
     * Bonus de points accordé pour une colonne du mur complètement pleine.
     */
    public static final int FULL_COLUMN_BONUS_POINTS = 7;

    /**
     * Bonus de points accordé pour avoir placé les 5 tuiles d'une même couleur sur le mur.
     */
    public static final int FULL_COLOR_BONUS_POINTS = 10;

    // --- Constantes pour la Ligne Plancher (Floor) ---

    // Le "4" n'est plus un magic number !
    private static final int BITS_PER_PENALTY = 4;
    private static final int PENALTY_MASK = (1 << BITS_PER_PENALTY) - 1; // 0xF

    /**
     * Pénalités individuelles pour les tuiles de 0 à 6 : {1, 1, 2, 2, 2, 3, 3}.
     * En hexadécimal, on l'écrit de droite à gauche.
     */
    private static final int FLOOR_PENALTY = 0x3322211;

    /**
     * Somme cumulée des pénalités pour 0 à 7 tuiles.
     * La multiplication par 0x1111111 crée une somme cumulée hexadécimale,
     * et le décalage (<< 4) aligne correctement les index.
     */
    private static final int TOTAL_FLOOR_PENALTY = (FLOOR_PENALTY * 0x1111111) << BITS_PER_PENALTY;


    /**
     * Calcule le nombre de points obtenus lors du placement d'une nouvelle tuile sur le mur.
            *
            * @param hGroupSize la taille du groupe horizontal auquel appartient la tuile (incluant la tuile)
     * @param vGroupSize la taille du groupe vertical auquel appartient la tuile (incluant la tuile)
     * @return le nombre de points marqués
     */
    public static int newWallTilePoints(int hGroupSize, int vGroupSize) {
        assert hGroupSize >= 1 && vGroupSize >= 1;

        if (hGroupSize != 1 && vGroupSize != 1) {
            return hGroupSize + vGroupSize;
        }
        return (hGroupSize == 1) ? vGroupSize : hGroupSize;
    }

    /**
     * Retourne la pénalité associée à la tuile située à l'index donné sur la ligne plancher.
     *
     * @param tileIndex l'index de la tuile sur la ligne plancher (0 à 6)
     * @return la valeur absolue de la pénalité (1, 2 ou 3)
     */
    public static int floorPenalty(int tileIndex) {
        assert tileIndex >= 0 && tileIndex < TileDestination.FLOOR.capacity();

        return (FLOOR_PENALTY >>> (tileIndex * BITS_PER_PENALTY)) & PENALTY_MASK;
    }

    /**
     * Retourne la pénalité totale associée à une ligne plancher contenant le nombre de tuiles donné.
     *
     * @param tilesCount le nombre de tuiles sur la ligne plancher (0 à 7)
     * @return la pénalité totale (valeur absolue entre 0 et 14)
     */
    public static int totalFloorPenalty(int tilesCount) {
        assert tilesCount >= 0 && tilesCount <= TileDestination.FLOOR.capacity();

        return (TOTAL_FLOOR_PENALTY >>> (tilesCount * BITS_PER_PENALTY)) & PENALTY_MASK;
    }
}
