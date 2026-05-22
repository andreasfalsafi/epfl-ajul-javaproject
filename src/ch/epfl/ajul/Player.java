package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

@FunctionalInterface
public interface Player {

    short nextMove(ReadOnlyGameState gameState);
}
