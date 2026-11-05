package ch.epfl.ajul;

import java.util.List;
import java.util.Objects;

/**
 * Représente une configuration de partie d'Ajul.
 * Cette classe est publique et immuable.
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class Game {

    /**
     * Enregistrement imbriqué permettant de décrire un joueur.
     * @param id l'identité du joueur
     * @param name le nom du joueur
     * @param kind la sorte du joueur
     */
    public record PlayerDescription(PlayerId id, String name, PlayerKind kind){

        /**
         * Type énuméré public représentant la sorte du joueur.
         */
        public enum PlayerKind{
            HUMAN, AI
        }

        /**
         * Constructeur compact qui vérifie qu'aucune des valeurs passées ne vaut null.
         * @throws NullPointerException si l'un des arguments est null
         */
        public PlayerDescription{
            Objects.requireNonNull(id);
            Objects.requireNonNull(name);
            Objects.requireNonNull(kind);
        }
    }

    private final List<PlayerDescription> playerDescriptions;

    private final int playersCount;

    private final List<PlayerId> playerIds;

    private final int factoriesCount;

    private final List<TileSource.Factory> factories;

    private final int tileSourcesCount;

    private final List<TileSource> tileSources;

    private final int centralAreaMaxSize;

    /**
     * Retourne une nouvelle configuration de partie pour les joueurs décrits.
     * @param playerDescriptionList la liste des descriptions des joueurs
     * @throws IllegalArgumentException si le nombre de joueurs n'est pas compris entre 2 et 4 inclus,
     * ou si la position d'un joueur dans la liste ne correspond pas à son identité (P1, P2, etc.)
     */
    public Game(List<PlayerDescription> playerDescriptionList){
        Preconditions.checkArgument(2 <= playerDescriptionList.size() && playerDescriptionList.size() <= 4);

        for (int i = 0; i < playerDescriptionList.size(); i++){
            Preconditions.checkArgument(playerDescriptionList.get(i).id() == PlayerId.ALL.get(i));
        }

        this.playersCount = playerDescriptionList.size();
        this.playerIds = PlayerId.ALL.subList(0, playersCount);
        this.playerDescriptions = List.copyOf(playerDescriptionList);

        this.factoriesCount = 2 * playersCount + 1;
        this.factories = TileSource.Factory.ALL.subList(0, factoriesCount);

        this.tileSourcesCount = factoriesCount + 1;
        this.tileSources = TileSource.ALL.subList(0, tileSourcesCount);

        this.centralAreaMaxSize = factoriesCount * 3 + 1;
    }

    /**
     * Retourne la liste (immuable) des descriptions des joueurs de la partie.
     * Le contenu est identique à celui de la liste passée au constructeur.
     * @return la liste des descriptions des joueurs
     */
    public List<PlayerDescription> playerDescriptions(){
        return playerDescriptions;
    }

    /**
     * Retourne la liste (immuable) des identités des joueurs de la partie.
     * Cette liste est un préfixe de PlayerId.ALL.
     * @return la liste des identités des joueurs
     */
    public List<PlayerId> playerIds(){
        return playerIds;
    }

    /**
     * Retourne le nombre de joueurs dans la partie.
     * @return le nombre de joueurs
     */
    public int playersCount(){
        return playersCount;
    }

    /**
     * Retourne la liste (immuable) des identités des fabriques utilisées dans la partie.
     * Cette liste est un préfixe de TileSource.Factory.ALL.
     * @return la liste des identités des fabriques
     */
    public List<TileSource.Factory> factories(){
        return factories;
    }

    /**
     * Retourne le nombre de fabriques utilisées dans la partie.
     * Pour une partie à n joueurs, ce nombre vaut 2n + 1.
     * @return le nombre de fabriques
     */
    public int factoriesCount(){
        return factoriesCount;
    }

    /**
     * Retourne la liste (immuable) des sources de tuiles utilisées dans la partie.
     * Cette liste est un préfixe de TileSource.ALL.
     * @return la liste des sources de tuiles
     */
    public List<TileSource> tileSources(){
        return tileSources;
    }

    /**
     * Retourne le nombre de sources de tuiles dans la partie.
     * Correspond au nombre de fabriques plus la zone centrale.
     * @return le nombre de sources de tuiles
     */
    public int tileSourcesCount(){
        return tileSourcesCount;
    }

    /**
     * Retourne le nombre maximum de tuiles pouvant se trouver dans la zone centrale durant la partie,
     * marqueur de premier joueur inclus.
     * Pour $m$ fabriques, ce nombre vaut $3m + 1$.
     * @return le nombre maximum de tuiles dans la zone centrale
     */
    public int centralAreaMaxSize(){
        return centralAreaMaxSize;
    }
}
