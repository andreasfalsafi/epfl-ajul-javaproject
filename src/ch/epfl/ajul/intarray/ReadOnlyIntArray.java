package ch.epfl.ajul.intarray;

/**
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 * Représente un tableau d'entiers en lecture seule.
 * <p>
 * Cette interface définit les opérations permettant d'accéder aux éléments
 * d'un tableau sans pouvoir les modifier. Elle facilite également la conversion
 * vers des structures immuables ou des tableaux standards.
 */
public interface ReadOnlyIntArray {

    /**
     * Retourne la taille (le nombre d'éléments) du tableau.
     * * @return la taille du tableau.
     */
    int size();

    /**
     * Retourne l'élément situé à l'index donné.
     * * @param i l'index de l'élément à récupérer.
     * @return l'entier situé à l'index {@code i}.
     * @throws IndexOutOfBoundsException si l'index est invalide (négatif ou supérieur ou égal à la taille).
     */
    int get(int i);

    /**
     * Retourne une version immuable de ce tableau.
     * <p>
     * Si le tableau est déjà immuable, cette méthode peut se retourner elle-même.
     * Sinon, elle retourne une copie dont le contenu ne pourra jamais changer.
     * * @return une instance de {@link ImmutableIntArray} contenant les mêmes éléments.
     */
    ImmutableIntArray immutable();

    /**
     * Retourne une copie des éléments de ce tableau sous la forme d'un tableau Java standard.
     * <p>
     * Les modifications apportées au tableau retourné n'affectent pas l'objet original.
     * * @return un nouveau tableau d'entiers ({@code int[]}) contenant tous les éléments.
     */
    int[] toArray();

}
