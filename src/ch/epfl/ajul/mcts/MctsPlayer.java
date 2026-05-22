package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.ArrayList;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.gamestate.Move.MAX_MOVES;

public final class MctsPlayer implements Player {
RandomGeneratorFactory random;
int numSims;


    public MctsPlayer(RandomGeneratorFactory random, int numSims){
        this.random = random;
        this.numSims = numSims;
    }

//créer les 3 tableaux
    //un int tab de 216
    //le tableau pour playersRank (un tableau de taille du nombre de joueur) (donc max 4, pas accès à game)
    //un arrayList<Byte> de taille encore inconnue.
    //
    //début algorithme
    //on crée un MCTSNODE RACINE (NewRoot)
    //
    //ensuite on cherche le coup à jouer
    //on copie GameState en mutableGameState

    // while numSims time>0;

    //current explored=racine;
    //while(current.a des enfants)
    //  int a = current.indexOfChildToExplore
    //  current = current.children[a]

    //on va dans cette node et ici, on modifie MutableGameState,
    //si ils existent pas, on créee des enfants.
    // et le troisième tableau,=> current player a joué en choisissant le coup x.
    //si mutableGameState indique que le round est fini, on "endround" avec comme argument random.seed.(current.pkMove).

    //Dans cette nouvelle node, simulation sans créer de node

    //  while partie pas finie
    //      simule en modifiant une mutablegameState et en faisant des coups avec HeuristicMoveSlector.selectMove
    //une fois que la boucle est finie, on chope le rang de chaque joueur dans cet état de partie.
    //endboucle

    // on distribue le nombre de point correspondant au joueur initial dans la node correspondant (donc, la dernière node prend les points du dernier joueurs
    //l'avant dernière prend les point de l'avant dernier joueurs, etc... etc... et elles gagnent toutes une simulation)

    // Node node = racine
    //int i = 0;
    // while(node != dernière)
    // Player = (arraylist.byte).player(i)
    //  node[(arraylist.byte).moveJoué].ajouter(RankPlayer(i));
    //
    // NewMove est finalement la node avec le plus d'itération.
    //
    public short nextMove(ReadOnlyGameState gameState) {
        short[] uniqueValidMovesArray = new short[MAX_MOVES];
        int[] playerRankings = new int[4];
        ArrayList<Byte> PlayersAndNodes = new ArrayList<>();

        MctsNode root = MctsNode.newRoot();

        RandomGenerator rng = random.create(gameState.pkTileBag());

        for(int i = 0; i < numSims; i++){
            MutableGameState modifiableGameState = new MutableGameState(gameState);
            PlayersAndNodes.clear();

            MctsNode currentExplored = root;

            //réel boucle
            //ordre joueur puis coup
            while(currentExplored.children != null){

                PlayersAndNodes.add((byte) modifiableGameState.currentPlayerId().ordinal());
                byte indexChild = (byte) Byte.toUnsignedInt((byte) currentExplored.indexOfChildToExplore());
                PlayersAndNodes.add(indexChild);
                short nextPkMove = currentExplored.children[indexChild].pkMove();
                modifiableGameState.registerMove( nextPkMove );
                currentExplored = currentExplored.children[indexChild];

                if(modifiableGameState.isRoundOver()){
                    modifiableGameState.endRound();
                    if(!modifiableGameState.isGameOver()) {
//                        modifiableGameState.fillFactories(random.create(nextPkMove));
                        modifiableGameState.fillFactories(rng);

                    }
                }
            }
            //currentExplored est donc notre node parente

            //créer les nodes
            int numValidMoves = modifiableGameState.uniqueValidMoves(uniqueValidMovesArray);

            if(numValidMoves > 0) {
                currentExplored.children = new MctsNode[numValidMoves];
                for (int j = 0; j < numValidMoves; j++) {
                    currentExplored.children[j] = MctsNode.newMoveNode(uniqueValidMovesArray[j]);
                }

                //on choisit une node depuis laquelle lancer la simulation
                int defaultChildren = 0;


//            if(currentExplored.children == null){
//                System.out.println("pas de coup pour l' ia.  " + currentExplored.children.length );
//            }


                currentExplored = currentExplored.children[defaultChildren];


                short simulatePkMove = currentExplored.pkMove();
                PlayersAndNodes.add((byte) modifiableGameState.currentPlayerId().ordinal());
                PlayersAndNodes.add((byte) defaultChildren);
                modifiableGameState.registerMove(simulatePkMove);
                if (modifiableGameState.isRoundOver()) {
                    modifiableGameState.endRound();
//                    modifiableGameState.fillFactories(random.create(simulatePkMove));
                    modifiableGameState.fillFactories(rng);

                }

            }
            //imagination
            while(!modifiableGameState.isGameOver()){

                if(modifiableGameState.isRoundOver()){
                    modifiableGameState.endRound();
                    modifiableGameState.fillFactories(rng);
                }
                else{
                    int numValidMoves2 = modifiableGameState.uniqueValidMoves(uniqueValidMovesArray);
                    int pkMovePlayed = HeuristicMoveSelector.selectMove(rng, modifiableGameState, uniqueValidMovesArray, numValidMoves2 );
                    modifiableGameState.registerMove(uniqueValidMovesArray[pkMovePlayed]);
                }
            }
            modifiableGameState.endGame();
            //modifiableGameState représente une partie finie (avec des point à attribuer)
            RankComputer.playersRank(modifiableGameState, playerRankings);
            int numPlayers = gameState.playerIds().size();
            for(int k = 0; k < numPlayers; k++){
                playerRankings[k] = (((numPlayers-1) - playerRankings[k]) << 8)
                        + PkPlayerStates.points(modifiableGameState.pkPlayerStates(), PlayerId.ALL.get(k));
            }

            //ajouter les points aux nodes correspondantes;

            MctsNode currentChild = root;
            root.registerEvaluation(0);
            for(int l = 0; l < PlayersAndNodes.size(); l += 2){
                int player = PlayersAndNodes.get(l);
                int playerIntMove = Byte.toUnsignedInt(PlayersAndNodes.get(l+1));
                currentChild = currentChild.children[playerIntMove];
                currentChild.registerEvaluation(playerRankings[player]);
            }

        }

        //"l'IA aurait dû retourner un index de zéro, car les branches liées à la racines étaient vides"
        assert(!(root.children == null || root.children.length == 0));

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
