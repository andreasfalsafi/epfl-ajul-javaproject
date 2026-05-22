package ch.epfl.ajul.mytests;

import ch.epfl.ajul.PlayerId;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MyPlayerIdTest {

    @Test
    void playerIdContainsCorrectValuesInOrder() {
        PlayerId[] expected = {PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4};
        assertArrayEquals(expected, PlayerId.values());
    }

    @Test
    void playerIdAllListIsCorrect() {
        List<PlayerId> expectedList = List.of(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4);
        assertEquals(expectedList, PlayerId.ALL);
    }
}