package ch.epfl.ajul.mcts;

/**
 * Représente un nœud de l'arbre de jeu utilisé par l'algorithme MCTS.
 * Chaque nœud stocke des informations sur les parties simulées (nombre de parties
 * et points cumulés) et peut posséder des enfants représentant les coups possibles.
 *
 * <p>La représentation interne utilise l'empaquetage binaire pour économiser la mémoire :
 * <ul>
 * <li>10 bits pour le coup ({@code pkMove})</li>
 * <li>22 bits pour le compteur de parties simulées ({@code N})</li>
 * </ul>
 *
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public final class MctsNode
    /** Coup empaqueté et compteur de parties simulées N. */{
    private int pkMoveN;

    /** Somme totale des points obtenus pour ce coup. */
    private int sum;

    /** Enfants du nœud (package-private pour accès par l'algorithme MCTS). */
    MctsNode[] children;

    private static final int N_SIZE = 22;
    private static final int N_MASK = (1 << N_SIZE) - 1;
    private static final int PKMOVE_MASK = (1 << 10 ) - 1;
    private static final short INVALID_MOVE = 0b1111111111;
    private static final int C = 80;

    private MctsNode(int pkMoveN,int sum){
        this.pkMoveN = pkMoveN;
        this.sum = sum;
        this.children = null;
    }

    private static int pkMoveNOf(int N, short pkmove){
        return N | pkmove << N_SIZE;
    }

    private static int pkMoveNGetN(int pkmoveN){
        return pkmoveN & N_MASK;
    }

    private static short pkMoveNGetPkMove(int pkmoveN){
        return (short)((pkmoveN >>> N_SIZE) & PKMOVE_MASK);
    }

    private static int pkMoveNaddN(int pkMoveN){
        assert(pkMoveNGetN(pkMoveN) != N_MASK);
        return pkMoveN+1;
    }

    /**
     * Crée un nouveau nœud racine.
     * @return un nouveau nœud racine initialisé.
     */
    public static MctsNode newRoot(){
        return new MctsNode(pkMoveNOf(1, INVALID_MOVE),0);
    }

    /**
     * Crée un nouveau nœud associé à un coup.
     * @param pkMove le coup empaqueté associé à ce nœud.
     * @return un nouveau nœud de coup.
     */
    public static MctsNode newMoveNode(short pkMove){
        return new MctsNode(pkMoveNOf(0, pkMove),0);
    }

    /** @return le coup empaqueté associé à ce nœud. */
    public short pkMove(){
        return pkMoveNGetPkMove(this.pkMoveN);
    }

    /** @return le nombre de parties simulées passant par ce nœud. */
    public int gameCount(){
        return pkMoveNGetN(this.pkMoveN);
    }


    /** @return la moyenne des points obtenus (S/N). */
    public double averagePoints(){
        return ((double) this.sum) / ((double) pkMoveNGetN(this.pkMoveN));
    }

    /**
     * Enregistre le résultat d'une simulation.
     * @param numPoints les points obtenus à la fin de la partie simulée.
     */
    public void registerEvaluation(int numPoints){
        this.pkMoveN = pkMoveNaddN(pkMoveN);
        this.sum += numPoints;
    }

    /**
     * Formule de calcul de priorité pour l'exploitation et l'exploration.
     * (formule du cours, on sors le log car il est constant, ce qui économise de la mémoire.)
     * @param children le nœud enfant à évaluer.
     * @param log le logarithme du nombre de parties du parent.
     * @return la valeur de priorité calculée.
     */
    private double formula(MctsNode children, double log){
        if(children.gameCount() == 0){
            return Double.POSITIVE_INFINITY;
        }
        double rightFormula = C * Math.sqrt((2 * log)
                / children.gameCount());
            return children.averagePoints() + rightFormula;
    }

    /**
     * Calcule l'index de l'enfant le plus prometteur selon la formule de priorité UCT.
     * @return l'index de l'enfant à explorer.
     */
    public int indexOfChildToExplore(){
        int simulated = pkMoveNGetN(this.pkMoveN);
        if(simulated <= children.length){
            return simulated - 1;
        }
        double log = Math.log(simulated);

        double currentBiggestFormula = formula(children[0], log);
        int index = 0;

        for(int i = 1; i< children.length; i++){
            double formulaNew = formula(children[i], log);
            if( formulaNew > currentBiggestFormula){
                currentBiggestFormula = formulaNew;
                index = i;
            }
        }
        return index;
    }
}

