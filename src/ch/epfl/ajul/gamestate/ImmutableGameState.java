package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;

import java.util.Objects;

/**
 * Représente un état complet et immuable d'une partie d'Ajul.
 *
 * @param game la configuration de la partie
 * @param pkTileBag le contenu empaqueté du sac de tuiles
 * @param pkTileSources le tableau immuable des sources de tuiles empaquetées
 * @param pkUniqueTileSources l'ensemble empaqueté des index des sources uniques
 * @param pkPlayerStates le tableau immuable des états empaquetés des joueurs
 * @param currentPlayerId l'identité du joueur courant
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public record ImmutableGameState(Game game, int pkTileBag, ImmutableIntArray pkTileSources, int pkUniqueTileSources, ImmutableIntArray pkPlayerStates, PlayerId currentPlayerId) implements ReadOnlyGameState {

    /**
     * Constructeur compact validant qu'aucun des arguments objets ne soit nul.
     * @throws NullPointerException si game, pkTileSources, pkPlayerStates ou currentPlayerId est null
     */
    public ImmutableGameState{
        Objects.requireNonNull(game);
        Objects.requireNonNull(pkTileSources);
        Objects.requireNonNull(pkPlayerStates);
        Objects.requireNonNull(currentPlayerId);
    }

    /**
     * Retourne l'état initial d'une partie d'Ajul.
     * Toutes les sources sont vides sauf la zone centrale qui contient le marqueur de premier joueur,
     * le sac contient la totalité des tuiles colorées, et le joueur courant est le premier joueur.
     *
     * @param game la configuration de la partie
     * @return l'état initial et immuable de la partie
     */
    public static ImmutableGameState initial(Game game){
        int[] pkTileSourcesArray = new int[game.tileSourcesCount()];
        pkTileSourcesArray[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        ImmutableIntArray allZerosPkPlayerStates = PkPlayerStates.initial(game);

        return new ImmutableGameState(game, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(pkTileSourcesArray), PkIntSet32.EMPTY, allZerosPkPlayerStates, PlayerId.P1);
    }

    /**
     * Retourne une version immuable de cet état.
     * Puisque cette classe est déjà immuable, elle retourne simplement le récepteur (this).
     *
     * @return cet état immuable (this)
     */
    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}
