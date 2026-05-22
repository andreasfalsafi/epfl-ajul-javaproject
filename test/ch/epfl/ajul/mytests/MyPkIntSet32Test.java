package ch.epfl.ajul.mytests;

import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyPkIntSet32Test {

    @Test
    void addAndContainsExtremeValues() {
        int set = PkIntSet32.EMPTY;

        // Ajout du bit de poids fort (index 31)
        // C'est souvent là que les décalages avec signe plantent
        set = PkIntSet32.add(set, 31);
        assertTrue(PkIntSet32.contains(set, 31), "Le 31ème bit n'a pas été correctement ajouté ou lu");
        assertEquals(1 << 31, set, "L'ajout de 31 devrait correspondre à Integer.MIN_VALUE (bit de signe)");

        // Ajout du bit 0
        set = PkIntSet32.add(set, 0);
        assertTrue(PkIntSet32.contains(set, 0));
        assertTrue(PkIntSet32.contains(set, 31));

        // Suppression
        set = PkIntSet32.remove(set, 31);
        assertFalse(PkIntSet32.contains(set, 31));
        assertTrue(PkIntSet32.contains(set, 0));
    }

    @Test
    void containsAllHandlesSubsetsCorrectly() {
        // Ensemble A : {1, 15, 30}
        int setA = (1 << 1) | (1 << 15) | (1 << 30);
        // Ensemble B : {1, 30}
        int setB = (1 << 1) | (1 << 30);
        // Ensemble C : {2}
        int setC = (1 << 2);

        assertTrue(PkIntSet32.containsAll(setA, setB), "setA contient setB");
        assertTrue(PkIntSet32.containsAll(setA, setA), "Un ensemble se contient lui-même");
        assertFalse(PkIntSet32.containsAll(setB, setA), "setB ne contient pas setA");
        assertFalse(PkIntSet32.containsAll(setA, setC), "setA ne contient pas setC");
        assertTrue(PkIntSet32.containsAll(setA, PkIntSet32.EMPTY), "Tout ensemble contient l'ensemble vide");
    }
}