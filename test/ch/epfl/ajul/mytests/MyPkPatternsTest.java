package ch.epfl.ajul.mytests;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyPkPatternsTest {
    private static final int EXAMPLE_PATTERNS = 0b00_000_000_100_011_000_011_000_010_010_001;

    @Test
    void emptyPatternIsZero() {
        assertEquals(0, PkPatterns.EMPTY);
    }

    @Test
    void sizeWorksCorrectly() {
        // En supposant que tes enums s'appellent PATTERN_1, PATTERN_2, etc. (index 0 à 4)
        assertEquals(1, PkPatterns.size(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_1));
        assertEquals(2, PkPatterns.size(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_2));
        assertEquals(3, PkPatterns.size(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_3));
        assertEquals(3, PkPatterns.size(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_4));
        assertEquals(0, PkPatterns.size(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void colorWorksCorrectly() {
        // Ligne 1 : couleur C, Ligne 2 : couleur A, Ligne 4 : couleur E
        assertEquals(TileKind.Colored.C, PkPatterns.color(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_1));
        assertEquals(TileKind.Colored.A, PkPatterns.color(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_2));
        assertEquals(TileKind.Colored.E, PkPatterns.color(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_4));
    }

    @Test
    void isFullWorksCorrectly() {
        // Ligne 1 a 1 tuile (max 1) -> true
        assertTrue(PkPatterns.isFull(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_1));
        // Ligne 2 a 2 tuiles (max 2) -> true
        assertTrue(PkPatterns.isFull(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_2));
        // Ligne 4 a 3 tuiles (max 4) -> false
        assertFalse(PkPatterns.isFull(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_4));
        // Ligne 5 a 0 tuile (max 5) -> false
        assertFalse(PkPatterns.isFull(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void canContainWorksCorrectly() {
        // Ligne 4 contient déjà des E, elle peut encore en accueillir (elle n'est pas pleine)
        assertTrue(PkPatterns.canContain(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_4, TileKind.Colored.E));
        // Ligne 4 ne peut pas accueillir de A (déjà assignée à E)
        assertFalse(PkPatterns.canContain(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_4, TileKind.Colored.A));
        // Ligne 5 est vide, elle peut accueillir n'importe quoi
        assertTrue(PkPatterns.canContain(EXAMPLE_PATTERNS, TileDestination.Pattern.PATTERN_5, TileKind.Colored.B));
    }

    @Test
    void toStringMatchesExample() {
        String expected = "[C, AA, AAA, EEE., .....]";
        assertEquals(expected, PkPatterns.toString(EXAMPLE_PATTERNS));
    }
}