package ch.epfl.ajul.intarray;

/**
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 * Une implémentation modifiable d'un tableau d'entiers.fonctions set sont accessibles en modifiatn le
 * tableau donné au constructeur ultérieurement.
 * <p>
 * Cette classe permet de modifier les éléments du tableau interne. Elle est conçue
 * pour offrir une vue structurée sur un tableau d'entiers existant sans forcément
 * effectuer de copie systématique.
 */
public final class MutableIntArray extends AbstractIntArray{

    /**
     * Constructeur privé pour limiter la création aux méthodes de fabrique statiques.
     * @param baseArray le tableau d'entiers à utiliser comme base.
     */
    private MutableIntArray(int[] baseArray) {
        super(baseArray);
    }

    /**
     * Crée une instance de {@link MutableIntArray} qui "emballe" le tableau fourni.
     * <p>
     * <b>Attention :</b> Cette méthode ne crée pas de copie. Toute modification
     * ultérieure du tableau original sera répercutée sur cette instance, et inversement.
     *
     * @param array le tableau d'entiers à emballer.
     * @return une nouvelle instance de {@link MutableIntArray} liée au tableau fourni.
     */
    public static MutableIntArray wrapping(int[]array){
        return new MutableIntArray(array);
    }


}
