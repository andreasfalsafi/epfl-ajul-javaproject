package ch.epfl.ajul.mytests;

import ch.epfl.ajul.mcts.MctsNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MctsNodeTest {

    @Test
    void newMoveNodePacksMoveAndEvaluationsCorrectly() {
        short fakeMove = 0b10_1010_1010; // Un coup empaqueté au hasard (10 bits max)
        MctsNode node = MctsNode.newMoveNode(fakeMove);

        assertEquals(fakeMove, node.pkMove(), "Le coup n'est pas correctement extrait après empaquetage");
        assertEquals(0, node.gameCount(), "Le compteur de parties d'un nouveau nœud doit être 0");
        assertEquals(0, node.totalPoints(), "Les points d'un nouveau nœud doivent être 0");

        // On simule quelques évaluations
        node.registerEvaluation(50);
        node.registerEvaluation(100);

        assertEquals(2, node.gameCount(), "Le compteur de parties doit s'incrémenter");
        assertEquals(150, node.totalPoints(), "Les points doivent s'accumuler");
        assertEquals(75.0, node.averagePoints(), 0.001, "La moyenne doit être en double (150 / 2)");
    }

    @Test
    void indexOfChildToExploreUsesOptimizationForUnexploredChildren() {
        MctsNode root = MctsNode.newRoot();
        // La racine a N=1 par défaut

        // On donne 3 enfants vierges (N=0) à la racine
        MctsNode[] children = new MctsNode[] {
                MctsNode.newMoveNode((short) 1),
                MctsNode.newMoveNode((short) 2),
                MctsNode.newMoveNode((short) 3)
        };
        // Attention : Adapte l'accès aux enfants selon ton implémentation (attribut package-private ou setter)
//        root.children = children;

        // Règle d'optimisation : si gameCount <= nombre d'enfants, 
        // on explore l'index = gameCount - 1

        // L'appel 1 : root a N=1. Il doit explorer l'enfant 0.
        assertEquals(0, root.indexOfChildToExplore());
        root.registerEvaluation(10); // root a maintenant N=2

        // L'appel 2 : root a N=2. Il doit explorer l'enfant 1.
        assertEquals(1, root.indexOfChildToExplore());
        root.registerEvaluation(20); // root a maintenant N=3

        // L'appel 3 : root a N=3. Il doit explorer l'enfant 2.
        assertEquals(2, root.indexOfChildToExplore());
    }
}