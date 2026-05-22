package ch.epfl.ajul;

import java.util.List;

/**
 * Représente une destination où les tuiles peuvent être placées sur le plateau d'un joueur.
 * <p>
 * Cette interface scellée définit les emplacements de réception des tuiles :
 * les lignes de motif (Pattern) ou la ligne de plancher (Floor).
 * Elle est implémentée par {@link Pattern} et {@link Floor}.
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public sealed interface TileDestination {

    /**
     * Représente les cinq lignes de motif (Pattern Lines) du plateau.
     * Chaque ligne a une capacité croissante selon son index.
     */
    enum Pattern implements TileDestination{
        PATTERN_1, PATTERN_2, PATTERN_3, PATTERN_4, PATTERN_5;

        /**
         * Retourne l'index de la ligne de motif.
         * @return l'index de la ligne (de 0 à 4).
         */
        @Override
        public int index() {
            return ordinal();
        }

        /**
         * Retourne la capacité maximale de la ligne de motif.
         * La capacité correspond à l'index de la ligne plus un.
         * @return la capacité de la ligne (de 1 à 5).
         */
        @Override
        public int capacity() {
            return ordinal() + 1;  //the index of the pattern line plus one returns the capacity of the line
        }

        /** Liste immuable de toutes les lignes de motif. */
        public static final List<Pattern> ALL = List.of(Pattern.values());

        /** Nombre total de lignes de motif. */
        public static final int COUNT = ALL.size();
    }

    /**
     * Représente la ligne de plancher (Floor Line) où sont placées les tuiles pénalisantes.
     */
    enum Floor implements TileDestination{
        FLOOR;

        /**
         * Retourne l'index unique de la ligne de plancher.
         * @return l'index 5.
         */
        @Override
        public int index() {
            return 5;
        }

        /**
         * Retourne la capacité maximale de la ligne de plancher.
         * @return la capacité fixe de 7 tuiles.
         */
        @Override
        public int capacity() {
            return 7;
        }
    }

    /**
     * Retourne l'index unique de la destination.
     * @return l'index (0-4 pour les motifs, 5 pour le plancher).
     */
    int index();

    /**
     * Retourne la capacité maximale de stockage de cette destination.
     * @return le nombre maximum de tuiles acceptées.
     */
    int capacity();
    /** Constante pour la première ligne de motif (capacité 1). */
    TileDestination PATTERN_1 = Pattern.PATTERN_1;
    /** Constante pour la deuxième ligne de motif (capacité 2). */
    TileDestination PATTERN_2 = Pattern.PATTERN_2;
    /** Constante pour la troisième ligne de motif (capacité 3). */
    TileDestination PATTERN_3 = Pattern.PATTERN_3;
    /** Constante pour la quatrième ligne de motif (capacité 4). */
    TileDestination PATTERN_4 = Pattern.PATTERN_4;
    /** Constante pour la cinquième ligne de motif (capacité 5). */
    TileDestination PATTERN_5 = Pattern.PATTERN_5;

    /** Constante pour la ligne de plancher (capacité 7). */
    TileDestination FLOOR = Floor.FLOOR;

    /** Liste immuable de toutes les destinations possibles sur le plateau. */
    List<TileDestination> ALL = List.of(PATTERN_1, PATTERN_2, PATTERN_3, PATTERN_4, PATTERN_5, FLOOR);

    /** Nombre total de destinations définies. */
    int COUNT = ALL.size();
}
