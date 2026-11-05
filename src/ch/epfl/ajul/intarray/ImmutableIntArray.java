package ch.epfl.ajul.intarray;

/**
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 * Une implémentation immuable d'un tableau d'entiers.
 * <p>
 * Une fois créée, le contenu de cette classe ne peut plus être modifié.
 * Elle est marquée comme {@code final} pour empêcher toute extension qui
 * pourrait introduire de la mutabilité.
 */
public final class ImmutableIntArray extends AbstractIntArray {

    /**
     * Constructeur privé pour garantir que la création passe par les méthodes de fabrique statiques.
     * @param baseArray le tableau d'entiers interne (doit déjà être une copie ou être protégé).
     */
    private ImmutableIntArray(int[] baseArray) {
        super(baseArray);
    }

    /**
     * Crée une instance immuable à partir d'un tableau d'entiers existant.
     * <p>
     * Cette méthode effectue une copie défensive du tableau fourni pour garantir
     * que des modifications ultérieures sur le tableau original n'affectent pas
     * cette instance.
     * * @param array le tableau d'origine à copier.
     * @return une nouvelle instance de {@link ImmutableIntArray} contenant les mêmes éléments.
     */
    public static ImmutableIntArray copyOf(int[] array){
        return new ImmutableIntArray(array.clone());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Comme cette instance est déjà immuable, cette méthode retourne simplement
     * {@code this} sans effectuer de copie supplémentaire, ce qui est très performant.
     * * @return cette instance elle-même.
     */
    @Override
    public ImmutableIntArray immutable() {
        return this;
    }
}
