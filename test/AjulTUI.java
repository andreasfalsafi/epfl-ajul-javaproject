import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import ch.epfl.ajul.mcts.MctsPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class AjulTUI {

    private static final Scanner SCANNER = new Scanner(System.in);

    // --- 1. AFFICHAGE DE L'ÉTAT DU JEU ---
    static void printState(ReadOnlyGameState gameState) {
        System.out.println("\n=========================================");
        ReadOnlyIntArray sources = gameState.pkTileSources();

        // Affichage des Fabriques
        System.out.print("Fabriques : ");
        for (int i = 1; i <= gameState.game().factoriesCount(); i++) {
            System.out.print("[" + i + "] " + PkTileSet.toString(sources.get(i)) + "    ");
            if (i % 3 == 0) System.out.println("\n            ");
        }

        // Affichage du Centre
        System.out.println("\n   Centre : [0] " + PkTileSet.toString(sources.get(0)));
        System.out.println("-----------------------------------------");

        // Affichage des Joueurs
        ReadOnlyIntArray playerStates = gameState.pkPlayerStates();
        for (PlayerId id : gameState.game().playerIds()) {
            int state = playerStates.get(id.ordinal());

            // Note : Adapte les noms si tes méthodes PkPlayerState s'appellent différemment
            int score = PkPlayerStates.points(playerStates, id);
            int patterns = PkPlayerStates.pkPatterns(playerStates, id);
            int wall = PkPlayerStates.pkWall(playerStates, id);
            int floor = PkPlayerStates.pkFloor(playerStates, id);

            System.out.println(">> Joueur " + id + " | " + score + " pts <<");
            System.out.println(" Motif    : " + PkPatterns.toString(patterns));
            System.out.println(" Mur      : " + PkWall.toString(wall));
            System.out.println(" Plancher : " + PkFloor.toString(floor));
            System.out.println();
        }
        System.out.println("=========================================\n");
    }

    // --- 2. DEMANDER LE COUP AU JOUEUR ---
    static Move queryNextMove(String playerName, ReadOnlyGameState gameState) {
        while (true) {
            System.out.print("Quel coup désirez-vous jouer, " + playerName + " ? ");
            String input = SCANNER.nextLine().trim().toUpperCase();

            // Le coup doit faire exactement 3 caractères (ex: "4B2")
            if (input.length() != 3) {
                System.out.println("❌ Format invalide. Exemple : 4B2 (Fabrique 4, Couleur B, Motif 2)");
                continue;
            }

            try {
                // Analyse du caractère 1 : La source
                int sIndex = input.charAt(0) - '0';
                TileSource source;
                if (sIndex == 0) {
                    source = TileSource.CenterArea.CENTER_AREA;
                } else if (sIndex >= 1 && sIndex <= gameState.game().factoriesCount()) {
                    source = TileSource.Factory.ALL.get(sIndex - 1);
                } else {
                    System.out.println("❌ Source invalide (doit être entre 0 et " + gameState.game().factoriesCount() + ").");
                    continue;
                }

                // Analyse du caractère 2 : La couleur
                char cChar = input.charAt(1);
                TileKind.Colored color = null;
                // On cherche la couleur dont le nom commence par la lettre tapée (A, B, C, D, E)
                for (TileKind.Colored c : TileKind.Colored.values()) {
                    if (c.name().charAt(0) == cChar) {
                        color = c;
                        break;
                    }
                }
                if (color == null) {
                    System.out.println("❌ Couleur invalide. Utilisez A, B, C, D ou E.");
                    continue;
                }

                // Analyse du caractère 3 : La destination
                int dIndex = input.charAt(2) - '0';
                TileDestination destination;
                if (dIndex == 0) {
                    destination = TileDestination.Floor.FLOOR;
                } else if (dIndex >= 1 && dIndex <= 5) {
                    destination = TileDestination.Pattern.ALL.get(dIndex - 1);
                } else {
                    System.out.println("❌ Destination invalide (0 pour plancher, 1-5 pour motif).");
                    continue;
                }



                // Si on arrive ici, le format est bon, on crée le coup
                return new Move(source, color, destination);

            } catch (Exception e) {
                System.out.println("❌ Erreur de lecture, essayez encore.");
            }
        }
    }

    // --- 3. BOUCLE PRINCIPALE ---
    // --- 3. BOUCLE PRINCIPALE ---
    public static void main(String[] args) {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();

        RandomGeneratorFactory factory = RandomGeneratorFactory.getDefault();
        int numSims = 10_000;
        MctsPlayer mctsAi = new MctsPlayer(factory, numSims);

        // 1. Création des joueurs (2 joueurs humains)
        List<Game.PlayerDescription> playerInfos = List.of(
                new Game.PlayerDescription(PlayerId.P1, "Aline", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P2, "Bertrand", Game.PlayerDescription.PlayerKind.AI)
        );

        Map<PlayerId, String> playerNames = Map.of(
                PlayerId.P1, "Morgan",
                PlayerId.P2, "Andre"
        );

        Game game = new Game(playerInfos);

        // 2. Initialisation de l'état
        MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game));
        gameState.fillFactories(randomGenerator);


        System.out.println("=== DÉBUT DE LA PARTIE D'AJUL ===");

        // 3. Boucle de jeu
        while (!gameState.isGameOver()) {
            printState(gameState);

            PlayerId currentPlayer = gameState.currentPlayerId();
            String playerName = playerNames.get(currentPlayer);

            // 1. On récupère les coups valides et LEUR NOMBRE
            short[] allValidMoves = new short[216]; // ou Move.MAX_MOVES si tu as la constante
            int validCount = gameState.validMoves(allValidMoves);

            Move move = null;
            boolean isValid = false;

            // On vérifie le type du joueur
            boolean isHuman = playerInfos.get(currentPlayer.ordinal()).kind() == Game.PlayerDescription.PlayerKind.HUMAN;

            if (isHuman) {
                // Le joueur est un Humain (ancien code conservé)
                while (!isValid) {
                    move = queryNextMove(playerName, gameState);
                    short packedMove = move.packed();

                    for (int i = 0; i < validCount; i++) {
                        if (allValidMoves[i] == packedMove) {
                            isValid = true;
                            break;
                        }
                    }

                    if (!isValid) {
                        System.out.println("❌ COUP INVALIDE ! Selon les règles...");
                    }
                }
            } else {
            // Le joueur est l'Ordinateur (MCTS)
            System.out.println("🤖 " + playerName + " (IA MCTS) réfléchit (" + numSims + " simulations)...");

            // On mesure le temps pour vérifier les perfs du projet
            long startTime = System.currentTimeMillis();

            // Ton MCTS calcule le meilleur coup
            short packedMove = mctsAi.nextMove(gameState);

            long endTime = System.currentTimeMillis();
            System.out.println("⏱ Temps de réflexion : " + (endTime - startTime) / 1000.0 + "s");

            move = Move.ofPacked(packedMove);
            isValid = true;

            System.out.println("🤖 " + playerName + " joue : " + String.format("%d%c%d",
                    move.source().index(),
                    move.tileColor().name().charAt(0),
                    move.destination().index()));
        }

            // 3. On enregistre le coup... (Suite du code existant)
            gameState.registerMove(move.packed());
            // Ligne de débug que tu avais ajoutée :
            // System.out.println("les unique tile sources après ce coup sont : "+ gameState.pkUniqueTileSources());

            // 4. Vérification de la fin de manche
            if (gameState.isRoundOver()) {
                System.out.println("\n*** MANCHE TERMINÉE : DÉCOMPTE DES POINTS ***");

                // On applique les points du mur et on retire les pénalités du plancher
                gameState.endRound();

                // --- NOUVEAU : Affichage des scores après le décompte ---
                System.out.println("Scores à la fin de cette manche :");
                for (PlayerId id : game.playerIds()) {
                    int currentScore = PkPlayerStates.points(gameState.pkPlayerStates(), id);
                    System.out.println(" -> " + playerNames.get(id) + " : " + currentScore + " points");
                }
                System.out.println("---------------------------------------------\n");
                // --------------------------------------------------------

                if (!gameState.isGameOver()) {
                    gameState.fillFactories(randomGenerator);
                }
            }
        }

        gameState.endGame(); // Ajoute les points bonus finaux (lignes, colonnes, couleurs pleines)

        printState(gameState);
        System.out.println("🎉 Partie terminée ! Scores finaux (avec bonus) :");
        for (PlayerId id : game.playerIds()) {
            int finalScore = PkPlayerStates.points(gameState.pkPlayerStates(), id);
            System.out.println(" -> " + playerNames.get(id) + " : " + finalScore + " points");
        }
    }
}