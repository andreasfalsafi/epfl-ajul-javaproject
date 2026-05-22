package ch.epfl.ajul.gamestate;

import org.junit.jupiter.api.Test;

import static ch.epfl.ajul.gamestate.Move.MAX_MOVES;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {
    @Test
    void MaxMovesIs216(){
        assertEquals(216,MAX_MOVES);
    }

}