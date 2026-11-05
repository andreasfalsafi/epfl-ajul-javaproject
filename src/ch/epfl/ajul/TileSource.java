package ch.epfl.ajul;

import java.util.List;

/**
 * Représente une source de tuiles dans le jeu (soit la zone centrale, soit une fabrique).
 * <p>
 * Cette interface scellée définit les emplacements où les joueurs peuvent puiser des tuiles.
 * Elle est implémentée par {@link CenterArea} et {@link Factory}.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public sealed interface TileSource {

    /**
     * Représente la zone centrale du jeu (le "Centre").
     * Il n'existe qu'une seule instance de cette zone.
     */
    enum CenterArea implements TileSource {
        CENTER_AREA;

        @Override
        public int index() {
            return 0;
        }
    }

    /**
     * Représente les différentes fabriques de tuiles présentes sur le plateau.
     * Les fabriques ont des index allant de 1 à 9.
     */
    enum Factory implements TileSource {
        FACTORY_1, FACTORY_2, FACTORY_3, FACTORY_4, FACTORY_5, FACTORY_6, FACTORY_7, FACTORY_8, FACTORY_9;

        /**
         * Nombre de tuiles déposées sur chaque fabrique lors du remplissage.
         */
        public static final int TILES_PER_FACTORY = 4;

        /**
         * Liste immuable de toutes les fabriques disponibles.
         */
        public static final List<Factory> ALL = List.of(Factory.values());

        /**
         * Nombre total de fabriques définies dans l'énumération.
         */
        public static final int COUNT = ALL.size();

        /**
         * Retourne l'index de la fabrique.
         * L'index est décalé de +1 pour laisser l'index 0 à la zone centrale.
         *
         * @return l'index de la fabrique (entre 1 et 9).
         */
        @Override
        public int index() {
            return ordinal() + 1;
        }


    }

    /**
     * Constante d'accès rapide pour la zone centrale.
     */
    TileSource CENTER_AREA = CenterArea.CENTER_AREA;

    /**
     * Constante pour la fabrique n°1.
     */
    TileSource FACTORY_1 = Factory.FACTORY_1;
    /**
     * Constante pour la fabrique n°2.
     */
    TileSource FACTORY_2 = Factory.FACTORY_2;
    /**
     * Constante pour la fabrique n°3.
     */
    TileSource FACTORY_3 = Factory.FACTORY_3;
    /**
     * Constante pour la fabrique n°4.
     */
    TileSource FACTORY_4 = Factory.FACTORY_4;
    /**
     * Constante pour la fabrique n°5.
     */
    TileSource FACTORY_5 = Factory.FACTORY_5;
    /**
     * Constante pour la fabrique n°6.
     */
    TileSource FACTORY_6 = Factory.FACTORY_6;
    /**
     * Constante pour la fabrique n°7.
     */
    TileSource FACTORY_7 = Factory.FACTORY_7;
    /**
     * Constante pour la fabrique n°8.
     */
    TileSource FACTORY_8 = Factory.FACTORY_8;
    /**
     * Constante pour la fabrique n°9.
     */
    TileSource FACTORY_9 = Factory.FACTORY_9;

    /**
     * Liste immuable contenant toutes les sources de tuiles (centre + fabriques).
     */
    List<TileSource> ALL = List.of(CENTER_AREA, FACTORY_1, FACTORY_2, FACTORY_3, FACTORY_4, FACTORY_5, FACTORY_6, FACTORY_7, FACTORY_8, FACTORY_9);

    /**
     * Nombre total de sources de tuiles disponibles.
     */
    int COUNT = ALL.size();

    /**
     * Retourne l'index unique de la source de tuiles.
     * L'index 0 est réservé au centre, et les index 1 à 9 aux fabriques.
     *
     * @return l'index de la source.
     */
    int index();

}
