package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Preconditions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyPreconditionsTest {

    @Test
    void checkArgumentDoesNotThrowOnTrue() {
        assertDoesNotThrow(() -> Preconditions.checkArgument(true));
    }

    @Test
    void checkArgumentThrowsOnFalse() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkArgument(false));
    }
}