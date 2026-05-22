package ch.epfl.ajul.mcts;

public final class MctsNode {
    private int pkMoveN;
    private int sum;
    //TODO RETIRER PUBLIC EN BAS Là
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

    public static MctsNode newRoot(){
        return new MctsNode(pkMoveNOf(1, INVALID_MOVE),0);
    }

    public static MctsNode newMoveNode(short pkMove){
        return new MctsNode(pkMoveNOf(0, pkMove),0);
    }

    public short pkMove(){
        return pkMoveNGetPkMove(this.pkMoveN);
    }

    public int gameCount(){
        return pkMoveNGetN(this.pkMoveN);
    }

//    public int totalPoints(){
//        return this.sum;
//    }

    public double averagePoints(){
        return ((double) this.sum) / ((double) pkMoveNGetN(this.pkMoveN));
    }

    public void registerEvaluation(int numPoints){
        this.pkMoveN = pkMoveNaddN(pkMoveN);
        this.sum += numPoints;
    }
    //private static double formula(int sumKid, int numGamesKid, int numGamesParent){
    //on sors le log car il est constant
    private double formula(MctsNode children, double log){
        if(children.gameCount() == 0){
            return Double.POSITIVE_INFINITY;
        }
        double rightFormula = C * Math.sqrt((2 * log)
                / children.gameCount());
            return children.averagePoints() + rightFormula;
    }

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

