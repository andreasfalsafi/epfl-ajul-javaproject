package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;

import java.util.ArrayList;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.gamestate.Move.MAX_MOVES;

/**
 * Représente un joueur contrôlé par une intelligence artificielle utilisant l'algorithme MCTS (Monte Carlo Tree Search).
 * <p>
 * Ce joueur détermine le meilleur coup à jouer en simulant un grand nombre de fins de parties
 * aléatoires pour construire un arbre de jeu partiel et évaluer les "points généralisés" moyens de chaque coup.
 * Cette classe est finale et implémente l'interface {@link Player}.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 *
 */

public final class MctsPlayer implements Player {

    private final RandomGeneratorFactory random;
    private final int numSims;

    /**
     * Construit un nouveau joueur simulé utilisant l'algorithme MCTS.
     *
     * @param random  la fabrique de générateurs aléatoires à utiliser pour créer les générateurs nécessaires aux simulations
     * @param numSims le nombre total d'itérations (ou parties simulées) à effectuer pour choisir le meilleur coup
     */

    public MctsPlayer(RandomGeneratorFactory random, int numSims){
        this.random = random;
        this.numSims = numSims;
    }

    /**
     * Détermine le meilleur coup à jouer pour l'état courant de la partie.
     * <p>
     * L'algorithme construit un arbre de jeu en répétant les opérations suivantes {@code numSims} fois :
     * <ol>
     * <li><b>Sélection et expansion :</b> Recherche depuis la racine un nœud non évalué et met à jour un état de jeu mutable.</li>
     * <li><b>Simulation :</b> Simule aléatoirement la fin de la partie en utilisant une heuristique prédéfinie.</li>
     * <li><b>Rétropropagation :</b> Calcule les points généralisés des joueurs (prenant en compte le rang et les points réels)
     * et les ajoute aux nœuds du chemin parcouru.</li>
     * </ol>
     *
     * @param gameState l'état actuel de la partie (en lecture seule)
     * @return le coup choisi comme étant le meilleur pour le joueur courant, sous sa forme empaquetée (packed)
     */
    public short nextMove(ReadOnlyGameState gameState) {
        short[] uniqueValidMovesArray = new short[MAX_MOVES];
        int[] playerRankings = new int[gameState.game().playersCount()];
        ArrayList<Byte> path = new ArrayList<>();

        MctsNode root = MctsNode.newRoot();

        RandomGenerator rng = random.create(gameState.pkTileBag());

        for(int i = 0; i < numSims; i++){
            MutableGameState simState = new MutableGameState(gameState);
            path.clear();

            MctsNode currentExplored = root;

            // 1. Recherche du nœud à évaluer (Descente dans l'arbre)
            while(currentExplored.children != null){

                path.add((byte) simState.currentPlayerId().ordinal());
                byte indexChild = (byte) currentExplored.indexOfChildToExplore();

                path.add(indexChild);
                short nextPkMove = currentExplored.children[indexChild].pkMove();
                simState.registerMove( nextPkMove );
                currentExplored = currentExplored.children[indexChild];

                if(simState.isRoundOver()){
                    endRoundAndFill(simState, rng);
                }
            }

            // 2. Création des enfants et expansion
            int numValidMoves = simState.uniqueValidMoves(uniqueValidMovesArray);

            if(numValidMoves > 0) {
                currentExplored.children = new MctsNode[numValidMoves];
                for (int j = 0; j < numValidMoves; j++) {
                    currentExplored.children[j] = MctsNode.newMoveNode(uniqueValidMovesArray[j]);
                }

                //on choisit une node depuis laquelle lancer la simulation
                int defaultChildren = 0;
                currentExplored = currentExplored.children[defaultChildren];


                short simulatePkMove = currentExplored.pkMove();
                path.add((byte) simState.currentPlayerId().ordinal());
                path.add((byte) defaultChildren);
                simState.registerMove(simulatePkMove);

                if (simState.isRoundOver()) {
                    endRoundAndFill(simState, rng);
                }
            }
            // 3. Simulation de la fin de la partie
            while(!simState.isGameOver()){

                if(simState.isRoundOver()){
                    endRoundAndFill(simState, rng);
                }
                else{
                    int numValidMoves2 = simState.uniqueValidMoves(uniqueValidMovesArray);
                    int pkMovePlayed = HeuristicMoveSelector.selectMove(rng, simState, uniqueValidMovesArray, numValidMoves2 );
                    simState.registerMove(uniqueValidMovesArray[pkMovePlayed]);
                }
            }
            simState.endGame();

            // 4. Calcul des points généralisés
            computeGeneralizedScores(simState, playerRankings);

            // 5. Rétropropagation des points aux nœuds
            MctsNode currentChild = root;
            root.registerEvaluation(0);
            for(int l = 0; l < path.size(); l += 2){
                int player = path.get(l);
                int playerIntMove = Byte.toUnsignedInt(path.get(l + 1));

                currentChild = currentChild.children[playerIntMove];
                currentChild.registerEvaluation(playerRankings[player]);
            }

        }

        assert (root.children != null && root.children.length > 0);

        // Sélection du meilleur coup basé sur la moyenne des points

        return getBestMove(root);
    }
    /**
     * Termine la manche et remplit les fabriques si la partie n'est pas finie.
     */
    private static void endRoundAndFill(MutableGameState state, RandomGenerator rng) {
        state.endRound();
        if (!state.isGameOver()) {
            state.fillFactories(rng);
        }
    }
    /**
     * Calcule les points généralisés pour tous les joueurs à la fin d'une partie simulée.
     * Le score combine le rang (inversé et décalé de 8 bits) et les points réels.
     */
    private static void computeGeneralizedScores(MutableGameState state, int[] rankings) {
        RankComputer.playersRank(state, rankings);
        int numPlayers = rankings.length;
        int maxRank = numPlayers - 1;

        for (int k = 0; k < numPlayers; k++) {
            int actualPoints = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.ALL.get(k));
            int invertedRank = maxRank - rankings[k];

            rankings[k] = (invertedRank << 8) + actualPoints;
        }
    }

    /**
     * Parcourt les enfants de la racine pour retourner le coup avec la meilleure moyenne de points.
     */
    private static short getBestMove(MctsNode root) {
        int indexOfChild = 0;
        double highestAvrgpoints = root.children[indexOfChild].averagePoints();

        for(int x = 0; x < root.children.length; x++){
            if(root.children[x].averagePoints() > highestAvrgpoints){
                highestAvrgpoints = root.children[x].averagePoints();
                indexOfChild = x;
            }
        }
        return root.children[indexOfChild].pkMove();
    }
}


