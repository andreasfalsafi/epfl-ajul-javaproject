package ch.epfl.ajul.mytests;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class MyPkPlayerStatesTest {

    @Test
    void readAndWriteWorkOnCorrectPlayerIndices() {
        // On crée un tableau modifiable pour 2 joueurs (taille 4 * 2 = 8)
        int[] states = new int[8];

        // On modifie l'état du joueur 2 (PlayerId.P2)
        // Les index pour P2 doivent être 4, 5, 6, 7
        PkPlayerStates.setPkPatterns(states, PlayerId.P2, 100);
        PkPlayerStates.setPkFloor(states, PlayerId.P2, 200);
        PkPlayerStates.setPkWall(states, PlayerId.P2, 300);
        PkPlayerStates.addPoints(states, PlayerId.P2, 15);
        PkPlayerStates.addPoints(states, PlayerId.P2, -5); // Test d'addition négative

        // On enveloppe le tableau en lecture seule (en supposant que ReadOnlyIntArray a une implémentation simple comme ImmutableIntArray)
        ReadOnlyIntArray readOnlyStates = ImmutableIntArray.copyOf(states);

        // --- Vérifications ---

        // Le joueur 1 (P1) doit être intact (des zéros partout)
        assertEquals(0, PkPlayerStates.pkPatterns(readOnlyStates, PlayerId.P1));
        assertEquals(0, PkPlayerStates.pkFloor(readOnlyStates, PlayerId.P1));
        assertEquals(0, PkPlayerStates.pkWall(readOnlyStates, PlayerId.P1));
        assertEquals(0, PkPlayerStates.points(readOnlyStates, PlayerId.P1));

        // Le joueur 2 (P2) doit avoir les bonnes valeurs
        assertEquals(100, PkPlayerStates.pkPatterns(readOnlyStates, PlayerId.P2));
        assertEquals(200, PkPlayerStates.pkFloor(readOnlyStates, PlayerId.P2));
        assertEquals(300, PkPlayerStates.pkWall(readOnlyStates, PlayerId.P2));
        assertEquals(10, PkPlayerStates.points(readOnlyStates, PlayerId.P2)); // 15 - 5 = 10
    }

    @Test
    void addPointsAccumulatesCorrectlyAndNeverGoesBelowZero() {
        // On crée un tableau d'états pour 2 joueurs (taille 4 * 2 = 8)
        int[] states = new int[8];

        // 1. Ajout de points basique
        PkPlayerStates.addPoints(states, PlayerId.P1, 15);

        // On vérifie directement dans le tableau primitif (P1 = index 0 à 3, les points sont à l'index 3)
        assertEquals(15, states[3], "Les points de P1 doivent être ajoutés à l'index 3");
        assertEquals(0, states[7], "Les points de P2 (index 7) ne doivent pas être modifiés");

        // 2. Accumulation (on ajoute encore des points au même joueur)
        PkPlayerStates.addPoints(states, PlayerId.P1, 10);
        assertEquals(25, states[3], "Les points doivent s'accumuler (15 + 10 = 25)");

        // 3. Soustraction (pénalité)
        PkPlayerStates.addPoints(states, PlayerId.P1, -5);
        assertEquals(20, states[3], "L'ajout d'un nombre négatif doit réduire le score (25 - 5 = 20)");

        // 4. LE PIÈGE : Le score d'Azul ne descend JAMAIS sous zéro !
        PkPlayerStates.addPoints(states, PlayerId.P2, 5); // P2 gagne 5 points
        PkPlayerStates.addPoints(states, PlayerId.P2, -14); // P2 prend un plancher complet (-14 points)

        assertEquals(0, states[7], "Le score de P2 ne doit pas devenir négatif, il doit être bloqué à 0 !");

        // On vérifie que P1 n'a pas été affecté par les pénalités de P2
        assertEquals(20, states[3], "Le score de P1 doit rester intact.");
    }
}