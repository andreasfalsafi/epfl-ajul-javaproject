package ch.epfl.ajul.mytests;

import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyPkTileSetTest {

    @Test
    void emptySetIsIndeedEmptyAndSizeZero() {
        int empty = PkTileSet.EMPTY;
        assertTrue(PkTileSet.isEmpty(empty));
        assertEquals(0, PkTileSet.size(empty));

        // On vérifie que le toString d'un ensemble vide est bien "{}"
        assertEquals("{}", PkTileSet.toString(empty));
    }

    @Test
    void fullSetHasCorrectSizeAndToString() {
        int full = PkTileSet.FULL;
        assertFalse(PkTileSet.isEmpty(full));

        // 20 tuiles * 5 couleurs + 1 marqueur = 101
        assertEquals(101, PkTileSet.size(full));

        String expectedToString = "{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}";
        assertEquals(expectedToString, PkTileSet.toString(full));
    }

    @Test
    void addAndRemoveWorksAsExpected() {
        int set = PkTileSet.EMPTY;

        // On ajoute une tuile A
        set = PkTileSet.add(set, TileKind.Colored.A);
        assertEquals(1, PkTileSet.countOf(set, TileKind.Colored.A));
        assertEquals(1, PkTileSet.size(set));

        // On retire la tuile A
        set = PkTileSet.remove(set, TileKind.Colored.A);
        assertEquals(0, PkTileSet.countOf(set, TileKind.Colored.A));
        assertTrue(PkTileSet.isEmpty(set));
    }

    @Test
    void unionAndDifferenceWorks() {
        int set1 = PkTileSet.of(2, TileKind.Colored.A); // {2*A}
        int set2 = PkTileSet.of(3, TileKind.Colored.B); // {3*B}

        int union = PkTileSet.union(set1, set2);
        assertEquals("{2*A,3*B}", PkTileSet.toString(union));

        int diff = PkTileSet.difference(union, set1);
        assertEquals("{3*B}", PkTileSet.toString(diff));
    }

    @Test
    void goodToStringPkTileSet(){
        assertEquals( "{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}", PkTileSet.toString(PkTileSet.FULL));
        assertEquals( "{}", PkTileSet.toString(PkTileSet.EMPTY));

    }

    @Test
    void emptySetIsTrulyEmpty() {
        assertEquals(0, PkTileSet.EMPTY);
        assertTrue(PkTileSet.isEmpty(PkTileSet.EMPTY));
        assertEquals(0, PkTileSet.size(PkTileSet.EMPTY));
    }

    @Test
    void fullSetHasCorrectSize() {
        assertEquals(101, PkTileSet.size(PkTileSet.FULL));
        assertFalse(PkTileSet.isEmpty(PkTileSet.FULL));
    }

    @Test
    void fullColoredSetHasCorrectSize() {
        assertEquals(100, PkTileSet.size(PkTileSet.FULL_COLORED));
    }

    @Test
    void ofCreatesSetCorrectly() {

        int set = PkTileSet.of(5, TileKind.Colored.A);
        assertEquals(5, PkTileSet.size(set));
        assertEquals(5, PkTileSet.countOf(set, TileKind.Colored.A));
        assertFalse(PkTileSet.isEmpty(set));

    }

    @Test
    void addAndRemoveWorkCorrectly() {

        int set = PkTileSet.EMPTY;
        set = PkTileSet.add(set, TileKind.Colored.B);
        assertEquals(1, PkTileSet.size(set));
        assertEquals(1, PkTileSet.countOf(set, TileKind.Colored.B));

        set = PkTileSet.remove(set, TileKind.Colored.B);
        assertTrue(PkTileSet.isEmpty(set));

    }

    @Test
    void unionAndDifferenceWorkCorrectly() {

        int set1 = PkTileSet.of(3, TileKind.Colored.A);
        int set2 = PkTileSet.of(2, TileKind.Colored.B);

        int unionSet = PkTileSet.union(set1, set2);
        assertEquals(5, PkTileSet.size(unionSet));
        assertEquals(3, PkTileSet.countOf(unionSet, TileKind.Colored.A));
        assertEquals(2, PkTileSet.countOf(unionSet, TileKind.Colored.B));

        int diffSet = PkTileSet.difference(unionSet, set2);
        assertEquals(set1, diffSet);

    }

    @Test
    void toStringFormatsProperly() {
        // Le format attendu pour FULL est défini dans la consigne
        String expected = "{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}";
        // Assure-toi que les noms A, B, C, D, E correspondent aux retours de la méthode name() de ton Enum
        assertEquals(expected, PkTileSet.toString(PkTileSet.FULL));
    }
}