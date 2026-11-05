package ch.epfl.ajul.intarray;

import java.util.Arrays;

/**
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 * Implémentation de base pour un tableau d'entiers en lecture seule.
 * <p>
 * Cette classe abstraite factorise les comportements communs aux tableaux
 * d'entiers, qu'ils soient immuables ou modifiables, en s'appuyant sur un
 * tableau Java standard interne.
 */
public abstract class AbstractIntArray implements ReadOnlyIntArray {

    /** Le tableau d'entiers interne stockant les données. */
    private int[] baseArray;

    /**
     * Construit un tableau d'entiers abstrait à partir du tableau fourni.
     * * @param baseArray le tableau d'entiers servant de source de données.
     */
    protected AbstractIntArray(int[] baseArray){
        this.baseArray = baseArray;
    }

    /**
     * {@inheritDoc}
     * @return le nombre d'éléments présents dans le tableau interne.
     */
    @Override
    public int size() {
        return baseArray.length;
    }

    /**
     * {@inheritDoc}
     * @param i l'index de l'élément à récupérer.
     * @return l'entier situé à l'index {@code i}.
     * @throws ArrayIndexOutOfBoundsException si l'index {@code i} est hors des limites du tableau.
     */
    @Override
    public int get(int i) {
        return baseArray[i];
    }

    /**
     * {@inheritDoc}
     * Retourne une copie immuable du contenu actuel.
     * @return une instance de {@link ImmutableIntArray} contenant les mêmes données.
     */
    @Override
    public ImmutableIntArray immutable() {
        return ImmutableIntArray.copyOf(baseArray);
    }

    /**
     * {@inheritDoc}
     * Cette méthode utilise {@link Object#clone()} pour garantir que le tableau
     * retourné est une copie indépendante.
     * @return une copie défensive du tableau interne sous forme de {@code int[]}.
     */
    @Override
    public int[] toArray() {
        return baseArray.clone();
    }

    /**
     * Retourne une représentation textuelle du contenu du tableau.
     * * @return une chaîne de caractères représentant les éléments,
     * par exemple "[1, 2, 3]".
     */
    @Override
    public String toString() {
        return Arrays.toString(baseArray);
    }
}
