package ch.epfl.ajul;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 * Représente les différents types de tuiles utilisables dans le jeu.
 * <p>
 * Cette interface est scellée et ne peut être implémentée que par
 * {@link Colored} pour les tuiles de jeu standards et
 * {@link FirstPlayerMarker} pour le marqueur de premier joueur.
 */
public sealed interface TileKind {

    /**
     * Types de tuiles colorées standards.
     */
    enum Colored implements TileKind {
        A, B, C, D, E;

        /** Liste immuable de tous les types de tuiles colorées. */
        public static final List<Colored> ALL = List.of(Colored.values());

        /** Nombre de types de tuiles colorées distinctes. =5 */
        public static final int COUNT = ALL.size();

        /**
         * Mélange un tableau de tuiles colorées en utilisant l'algorithme de Fisher-Yates.
         *
         * @param tiles           le tableau de tuiles à mélanger
         * @param randomGenerator le générateur de nombres aléatoires à utiliser
         */
        public static void shuffle(Colored[] tiles, RandomGenerator randomGenerator){
            for (int i = 0; i < tiles.length - 1; i++){
                int j = randomGenerator.nextInt(i, tiles.length);
                Colored temp = tiles[i];
                tiles[i] = tiles[j];
                tiles[j] = temp;
            }
        }

        @Override
        public int index() {
            return ordinal();
        }

        @Override
        public int tilesCount() {
            return 20;
        }

    }

    /**
     * Représente le marqueur spécial désignant le premier joueur du prochain tour.
     */
    enum FirstPlayerMarker implements TileKind{
        FIRST_PLAYER_MARKER;

        @Override
        public int index() {
            return 5;
        }

        @Override
        public int tilesCount() {
            return 1;
        }
    }

    /** Constante pour le type de tuile colorée A. */
    TileKind A = Colored.A;
    /** Constante pour le type de tuile colorée B. */
    TileKind B = Colored.B;
    /** Constante pour le type de tuile colorée C. */
    TileKind C = Colored.C;
    /** Constante pour le type de tuile colorée D. */
    TileKind D = Colored.D;
    /** Constante pour le type de tuile colorée E. */
    TileKind E = Colored.E;
    /** Le marqueur de premier joueur accessible comme un type de tuile. */
    TileKind FIRST_PLAYER_MARKER = FirstPlayerMarker.FIRST_PLAYER_MARKER;

    /** Liste immuable contenant tous les types de tuiles (colorées et marqueur). */
    List<TileKind> ALL = List.of(A, B, C, D, E, FIRST_PLAYER_MARKER);

    /** Nombre total de types de tuiles différents définis dans le système. =6 */
    int COUNT = ALL.size();

    /**
     * Retourne l'index unique associé au type de tuile.
     * @return l'index (de 0 à 4 pour les tuiles colorées, 5 pour le marqueur)
     */
    int index();

    /**
     * Retourne le nombre total de tuiles de ce type présentes au début d'une partie.
     * @return le nombre de tuiles physiques existantes pour ce type
     */
    int tilesCount();

}
