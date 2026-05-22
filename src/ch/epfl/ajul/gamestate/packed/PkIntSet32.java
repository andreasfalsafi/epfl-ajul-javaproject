package ch.epfl.ajul.gamestate.packed;

/**
 * Contient des méthodes statiques permettant de manipuler des ensembles d'entiers
 * compris entre 0 et 31 (inclus), empaquetés dans une valeur de type int.
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class PkIntSet32 {

    /**
     * Représente l'ensemble vide (aucun bit n'est à 1).
     */
    public static final int EMPTY = 0;
    
    private static final int BIT_MASK = 0b1;

    private static boolean isValid(int i){
        return (i >= 0 && i <= 31);
    }

    /**
     * Retourne vrai si et seulement si l'ensemble empaqueté contient l'élément donné.
     *
     * @param pkIntSet32 l'ensemble d'entiers empaqueté
     * @param i l'élément dont on veut vérifier la présence
     * @return true si l'élément est présent, false sinon
     */
    public static boolean contains(int pkIntSet32, int i){
        assert(isValid(i));
        return((pkIntSet32 >>> i) & BIT_MASK) == 1;
    }

    /**
     * Retourne vrai si et seulement si le premier ensemble empaqueté contient
     * tous les éléments du second ensemble empaqueté (inclusion au sens large).
     *
     * @param pkIntSet32a le premier ensemble (le contenant potentiel)
     * @param pkIntSet32b le second ensemble (le contenu potentiel)
     * @return true si l'inclusion est vérifiée, false sinon
     */
    public static boolean containsAll(int pkIntSet32a, int pkIntSet32b){
        return (pkIntSet32a & pkIntSet32b) == pkIntSet32b;
    }

    /**
     * Retourne l'ensemble empaqueté identique à celui donné, mais
     * auquel l'élément spécifié a été ajouté.
     *
     * @param pkIntSet32 l'ensemble d'entiers empaqueté initial
     * @param i l'élément à ajouter
     * @return le nouvel ensemble empaqueté
     */
    public static int add(int pkIntSet32, int i){
        assert(isValid(i));
        return pkIntSet32 | (BIT_MASK << i);
    }

    /**
     * Retourne l'ensemble empaqueté identique à celui donné, mais
     * duquel l'élément spécifié a été retiré.
     *
     * @param pkIntSet32 l'ensemble d'entiers empaqueté initial
     * @param i l'élément à retirer
     * @return le nouvel ensemble empaqueté
     */
    public static int remove(int pkIntSet32, int i){
        assert(isValid(i));
        return pkIntSet32 & ~(BIT_MASK << i);
    }
}
