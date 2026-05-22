package ch.epfl.ajul;

/**
 * Classe utilitaire facilitant l'écriture et la vérification des préconditions.
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class Preconditions {

    /**
     * Vérifie que l'argument passé (la précondition) est vrai.
     * Lève l'exception IllegalArgumentException si son argument est faux, et ne fait rien sinon.
     *
     * @param shouldBeTrue l'expression booléenne qui doit être évaluée
     * @throws IllegalArgumentException si l'argument shouldBeTrue est faux
     */
    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }

}
