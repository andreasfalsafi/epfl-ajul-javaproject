package ch.epfl.ajul.mytests;

import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPkFloorTest {

    // Représentation de la Figure 2 de l'énoncé
    // 00000000 000 000 000 000 001 001 101 011
    private static final int EXAMPLE_FLOOR = 0b00000000_000_000_000_000_001_001_101_011;

    @Test
    void emptyFloorIsZero() {
        assertEquals(0, PkFloor.EMPTY);
    }

    @Test
    void sizeWorksCorrectly() {
        assertEquals(3, PkFloor.size(EXAMPLE_FLOOR));
    }

    @Test
    void tileAtWorksCorrectly() {
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(EXAMPLE_FLOOR, 0));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(EXAMPLE_FLOOR, 1));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(EXAMPLE_FLOOR, 2));
    }

    @Test
    void containsFirstPlayerMarkerWorks() {
        assertTrue(PkFloor.containsFirstPlayerMarker(EXAMPLE_FLOOR));
        assertFalse(PkFloor.containsFirstPlayerMarker(PkFloor.EMPTY));
    }

    @Test
    void toStringMatchesExample() {
        String expected = "[FIRST_PLAYER_MARKER, B, B]";
        assertEquals(expected, PkFloor.toString(EXAMPLE_FLOOR));
    }

    @Test
    void withAddedTilesRespectsCapacityAndMarkerRule() {

        int floor = PkFloor.EMPTY;
        int sixA = PkTileSet.of(6, TileKind.Colored.A);
        int bad = PkTileSet.of(12,TileKind.Colored.B );
        int terrible = PkTileSet.of(1,TileKind.FIRST_PLAYER_MARKER);
        int terriblyBad= PkTileSet.union(bad,terrible);
        int test = PkTileSet.union(sixA,terriblyBad);

        floor = PkFloor.withAddedTiles(floor, test);


        int expectedfloor= 0b0000_0000_101_000_000_000_000_000_000_111;


        int newFloor = floor;
        assertEquals(7, PkFloor.size(newFloor));
        assertTrue(PkFloor.containsFirstPlayerMarker(newFloor));
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(newFloor, 6));
        assertEquals(expectedfloor,newFloor);
    }

    @Test
    void withAddedTilesRespectsCapacityAndMarkerRule2() {

        int floor = PkFloor.EMPTY;
        int sixA = PkTileSet.of(6, TileKind.Colored.A);
        int bad = PkTileSet.of(12,TileKind.Colored.B );
        int test = PkTileSet.union(sixA,bad);

        floor = PkFloor.withAddedTiles(floor, test);


        int expectedfloor= 0b0000_0000_001_000_000_000_000_000_000_111;


        int newFloor = floor;
        assertEquals(7, PkFloor.size(newFloor));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(newFloor, 6));
        assertEquals(expectedfloor,newFloor);
    }

    @Test
    void withAddedTilesSortsTruncatesAndForcesMarkerCorrectly() {
        // État initial : 5 tuiles 'B' (index de sorte = 1)
        // B, B, B, B, B
        int floor = 0b0000_0000_000_000_001_001_001_001_001_101;

        // Création d'un ensemble à ajouter : 3 'C', 2 'D' et 1 FIRST_PLAYER_MARKER
        int threeC = PkTileSet.of(3, TileKind.Colored.C);
        int twoD = PkTileSet.of(2, TileKind.Colored.D);
        int marker = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int toAdd = PkTileSet.union(threeC, PkTileSet.union(twoD, marker));

        // Exécution
        int newFloor = PkFloor.withAddedTiles(floor, toAdd);

        // Ce qui doit se passer :
        // 1. Les 'C' sont traités en premier. Il reste 2 places (7 - 5).
        //    On ajoute deux 'C'. La ligne est pleine. Le 3ème 'C' part à la poubelle.
        //    État temporaire : [B, B, B, B, B, C, C]
        // 2. Les 'D' sont traités. La ligne est pleine, ils sont ignorés.
        // 3. Le marqueur est traité. Il DOIT rentrer. Il prend la place de la dernière tuile (le 2ème 'C').
        // Résultat attendu : [B, B, B, B, B, C, FIRST_PLAYER_MARKER]

        // FIRST_PLAYER_MARKER = 5 (101), C = 2 (010), B = 1 (001), Taille = 7 (111)
        int expectedFloor = 0b0000_0000_101_010_001_001_001_001_001_111;

        assertEquals(7, PkFloor.size(newFloor), "La taille doit être capée à 7");
        assertTrue(PkFloor.containsFirstPlayerMarker(newFloor), "Le marqueur doit être présent");
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(newFloor, 6), "Le marqueur doit être à la dernière position");
        assertEquals(TileKind.Colored.C, PkFloor.tileAt(newFloor, 5), "L'avant-dernière tuile doit être un C");
        assertEquals(expectedFloor, newFloor, "La représentation binaire exacte ne correspond pas");
    }

    @Test
    void withAddedTilesForcesMarkerOnAlreadyFullFloor() {
        // État initial : Plancher plein à craquer avec [A, B, C, D, E, A, B]
        // Indices : A=0, B=1, C=2, D=3, E=4
        // Binaire : 8 zéros | 001(B) | 000(A) | 100(E) | 011(D) | 010(C) | 001(B) | 000(A) | 111(Taille 7)
        int fullFloor = 0b0000_0000_001_000_100_011_010_001_000_111;

        // On n'ajoute QUE le marqueur de premier joueur
        int markerOnly = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        // Exécution
        int newFloor = PkFloor.withAddedTiles(fullFloor, markerOnly);

        // Ce qui doit se passer :
        // Le plancher est déjà à 7. Le marqueur éjecte la tuile à l'index 6 (qui était un 'B' / 001).
        // Le reste ne bouge pas.
        // Résultat attendu : [A, B, C, D, E, A, FIRST_PLAYER_MARKER]
        int expectedFloor = 0b0000_0000_101_000_100_011_010_001_000_111;

        assertEquals(7, PkFloor.size(newFloor), "La taille doit rester à 7");
        assertTrue(PkFloor.containsFirstPlayerMarker(newFloor), "Le marqueur a dû écraser la dernière tuile");
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(newFloor, 6), "Le marqueur occupe l'index 6");
        assertEquals(TileKind.Colored.A, PkFloor.tileAt(newFloor, 5), "L'index 5 ne doit pas avoir été modifié");
        assertEquals(expectedFloor, newFloor, "La représentation binaire exacte ne correspond pas");
    }



}
