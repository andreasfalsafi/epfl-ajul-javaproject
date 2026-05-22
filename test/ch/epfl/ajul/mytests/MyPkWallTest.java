package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Points;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class MyPkWallTest {

    @Test
    void pkWallToStringWorksOnEmptyWall(){
        String calculated= PkWall.toString(PkWall.EMPTY);
        String expected ="[abcde, eabcd, deabc, cdeab, bcdea]";

        assertEquals(expected,calculated);
    }
    @Test
    void pkWallToStringWorksOnWall(){
        String calculated= PkWall.toString(0b00000001111101000010100001001101);
        String expected ="[AbCDe, eAbcd, dEaBc, cdeAb, BCDEA]";

        assertEquals(expected,calculated);
    }

    @Test
    void wallColorAndIndexMappingAreStrictlyCorrect() {
        // On vérifie la diagonale principale (qui doit être de couleur A, index 0, 6, 12, 18, 24)
        // Ligne 1, Col 0 -> A
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 0));
        assertEquals(0, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));

        // Ligne 2, Col 1 -> A (décalage vers la droite)
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 1));
        assertEquals(6, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.A));

        // Ligne 5, Col 4 -> A
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 4));
        assertEquals(24, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.A));

        // Un test de l'enroulement (wrap-around) : Ligne 2, Col 0 -> E
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 0));
        assertEquals(5, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.E));
    }

    @Test
    void hGroupSizeAndVgroupSizeWorksOnFullWall(){
        int WALL= 0b00000001111111111111111111111111;
        int empty = PkWall.EMPTY;
        boolean result= true;
        for(int i = 0; i<5; i++){
            for(int j =0;j<5;j++){
                TileDestination.Pattern line = TileDestination.Pattern.ALL.get(i);
                TileKind.Colored color = TileKind.Colored.ALL.get(j);
                if (PkWall.vGroupSize(WALL, line, color) !=5){
                    result=false;
                }
                if (PkWall.hGroupSize(WALL, line, color) !=5){
                    result=false;
                }
            }
        }
        for(int i = 0; i<5; i++){
            for(int j =0;j<5;j++){
                TileDestination.Pattern line = TileDestination.Pattern.ALL.get(i);
                TileKind.Colored color = TileKind.Colored.ALL.get(j);
                if (PkWall.vGroupSize(empty, line, color) !=0){
                    result=false;
                }
                if (PkWall.hGroupSize(empty, line, color) !=0){
                    result=false;
                }
            }
        }
        assertTrue(result);
    }

    @Test
    void hGroupSizeAndVgroupSizeWorksOnOtherWall(){
        int WALL= 0b00000001110111110011110111011011;

        assertEquals(2, PkWall.hGroupSize(WALL, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
        assertEquals(PkWall.hGroupSize(WALL, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A), PkWall.hGroupSize(WALL, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B));
        assertEquals(0, PkWall.hGroupSize(WALL, TileDestination.Pattern.PATTERN_1, TileKind.Colored.C));
        System.out.println(PkWall.toString(WALL));
        assertEquals(4, PkWall.vGroupSize(WALL, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B));
        int WALL2= 0b0000000_10000_01001_00101_00010_00001;
        assertEquals(1, PkWall.vGroupSize(WALL2, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
        assertEquals(2, PkWall.vGroupSize(WALL2, TileDestination.Pattern.PATTERN_3, TileKind.Colored.D));
        assertEquals(7, Points.newWallTilePoints(PkWall.hGroupSize(WALL, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B), PkWall.vGroupSize(WALL, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B) ));
    }

    @Test
    void hGroupSizeCalculatesContiguousTilesAndStopsAtGaps() {
        // Mur avec la ligne 3 (PATTERN_3) configurée comme suit : [X, empty, X, X, empty]
        // Les index pour la ligne 3 vont de 10 à 14.
        // On place des tuiles aux index 10 (Col 0), 12 (Col 2), 13 (Col 3).
        int wall = PkWall.EMPTY;

        // On ajoute les tuiles sans se soucier de la couleur exacte en récupérant la bonne couleur de la colonne
        wall = PkIntSet32.add(wall, 10);
        wall = PkIntSet32.add(wall, 12);
        wall = PkIntSet32.add(wall, 13);

        // Si on interroge le groupe de la tuile en index 12 (Col 2)
        TileKind.Colored colorAt12 = PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 2);

        // Le groupe horizontal autour de 12 contient 12 et 13 -> taille 2
        // Il ne doit pas franchir le trou à l'index 11 pour inclure l'index 10.
        assertEquals(2, PkWall.hGroupSize(wall, TileDestination.Pattern.PATTERN_3, colorAt12),
                "Le groupe horizontal ne doit pas traverser les cases vides");

        // Si on interroge la tuile isolée en 10
        TileKind.Colored colorAt10 = PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 0);
        assertEquals(1, PkWall.hGroupSize(wall, TileDestination.Pattern.PATTERN_3, colorAt10),
                "La tuile isolée doit avoir un hGroupSize de 1");
    }

    @Test
    void withTileAtDoesNotAddImpossibleTile(){
        int fullPkIntSet32 = 0b00000001111111111111111111110111;
        fullPkIntSet32 = PkIntSet32.add(fullPkIntSet32, 3);
        System.out.println(fullPkIntSet32);

    }

    @Test
    void isColorFullUsesCorrectMasks() {
        int wall = PkWall.EMPTY;

        // On va remplir toutes les cases correspondant à la couleur C.
        // Motif de la figure 1 pour la couleur C :
        // Ligne 1 -> Col 2 (index 2)
        // Ligne 2 -> Col 3 (index 8)
        // Ligne 3 -> Col 4 (index 14)
        // Ligne 4 -> Col 0 (index 15)
        // Ligne 5 -> Col 1 (index 21)
        wall = PkIntSet32.add(wall, 2);
        wall = PkIntSet32.add(wall, 8);
        wall = PkIntSet32.add(wall, 14);
        wall = PkIntSet32.add(wall, 15);

        assertFalse(PkWall.isColorFull(wall, TileKind.Colored.C), "Il manque encore une tuile C");

        // Ajout de la dernière
        wall = PkIntSet32.add(wall, 21);

        assertTrue(PkWall.isColorFull(wall, TileKind.Colored.C), "Toutes les cases C sont remplies");
        assertFalse(PkWall.isColorFull(wall, TileKind.Colored.B), "Les cases B ne sont pas remplies");
    }

    @Test
    void hGroupSizeMustNotWrapAroundToAdjacentRows() {
        int wall = PkWall.EMPTY;

        // On place des tuiles aux index 3 et 4 (fin de la PATTERN_1)
        wall = PkIntSet32.add(wall, 3);
        wall = PkIntSet32.add(wall, 4); // Tuile de couleur E

        // On place des tuiles aux index 5 et 6 (début de la PATTERN_2)
        wall = PkIntSet32.add(wall, 5); // Tuile de couleur E
        wall = PkIntSet32.add(wall, 6);

        // En binaire, les bits 3, 4, 5 et 6 sont tous à 1 à la suite (0b01111000).
        // Mais physiquement, le mur ressemble à ça :
        // [ . . . X X ]
        // [ X X . . . ]
        // [ . . . . . ] ...

        // Si on demande la taille du groupe de la tuile en index 4 (PATTERN_1, couleur E)
        // La réponse DOIT être 2 (les tuiles 3 et 4).
        // Si ton code renvoie 4, c'est que tu as "débordé" sur la ligne 2 à cause du bit 5 !
        assertEquals(2, PkWall.hGroupSize(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.E),
                "hGroupSize déborde sur la ligne suivante !");

        // Pareil pour le début de la ligne 2 (index 5, couleur E)
        assertEquals(2, PkWall.hGroupSize(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.E),
                "hGroupSize déborde sur la ligne précédente !");
    }

    @Test
    void vGroupSizeStopsAtGapsAndDoesNotWrap() {
        int wall = PkWall.EMPTY;

        // Colonne 2 (index 2, 7, 12, 17, 22)
        // On place des tuiles en 2, 7 ... et 17, 22. On laisse la 12 vide !
        wall = PkIntSet32.add(wall, 2);
        wall = PkIntSet32.add(wall, 7);  // PATTERN_2, Col 2 -> Couleur B
        // Trous en 12
        wall = PkIntSet32.add(wall, 17);
        wall = PkIntSet32.add(wall, 22);

        System.out.println(PkWall.toString(wall));

        // Groupe vertical pour l'index 7 : il doit monter vers 2 (donc 2 tuiles),
        // mais il ne doit PAS descendre à 17 car l'index 12 est vide !
        assertEquals(2, PkWall.vGroupSize(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B),
                "vGroupSize a traversé une case vide ou mal calculé le pas de 5 !");

        // Groupe vertical pour l'index 17 : doit inclure 22, mais pas 7.
        // PATTERN_4, Col 2 -> Couleur D
        assertEquals(2, PkWall.vGroupSize(wall, TileDestination.Pattern.PATTERN_4, TileKind.Colored.E),
                "vGroupSize a traversé une case vide vers le haut !");
    }

    @Test
    void isColorFullFailsIfEvenOneTileIsMissingDespiteNoise() {
        // Le masque complet pour la couleur A est composé des index : 0, 6, 12, 18, 24
        // Binaire attendu : 0b00000001_00000100_00010000_01000001

        // On crée un mur avec TOUTES les cases remplies... SAUF l'index 12 (PATTERN_3, A)
        // ~ (1 << 12) crée un masque où tous les bits sont à 1, sauf le 12ème.
        int almostFullWall = PkIntSet32.EMPTY;
        for (int i = 0; i < 25; i++) {
            if (i != 12) {
                almostFullWall = PkIntSet32.add(almostFullWall, i);
            }
        }

        // Toutes les autres couleurs DOIVENT être pleines
        assertTrue(PkWall.isColorFull(almostFullWall, TileKind.Colored.B));
        assertTrue(PkWall.isColorFull(almostFullWall, TileKind.Colored.C));
        assertTrue(PkWall.isColorFull(almostFullWall, TileKind.Colored.D));
        assertTrue(PkWall.isColorFull(almostFullWall, TileKind.Colored.E));

        // Mais A doit retourner false car il lui manque pile son centre (index 12)
        assertFalse(PkWall.isColorFull(almostFullWall, TileKind.Colored.A),
                "isColorFull a ignoré une case manquante à cause du 'bruit' des autres tuiles !");
    }

    @Test
    void asPkTileSetExtractsCorrectCountsFromComplexWall() {
        int wall = PkWall.EMPTY;

        // On va placer :
        // 1 tuile A (index 0)
        // 2 tuiles B (index 1, 7)
        // 3 tuiles C (index 2, 8, 14)
        // 4 tuiles D (index 3, 9, 10, 16)
        // 5 tuiles E (index 4, 5, 11, 17, 23)

        wall = PkIntSet32.add(wall, 0); // A

        wall = PkIntSet32.add(wall, 1); // B
        wall = PkIntSet32.add(wall, 7); // B

        wall = PkIntSet32.add(wall, 2); // C
        wall = PkIntSet32.add(wall, 8); // C
        wall = PkIntSet32.add(wall, 14);// C

        wall = PkIntSet32.add(wall, 3); // D
        wall = PkIntSet32.add(wall, 9); // D
        wall = PkIntSet32.add(wall, 10);// D
        wall = PkIntSet32.add(wall, 16);// D

        wall = PkIntSet32.add(wall, 4); // E
        wall = PkIntSet32.add(wall, 5); // E
        wall = PkIntSet32.add(wall, 11);// E
        wall = PkIntSet32.add(wall, 17);// E
        wall = PkIntSet32.add(wall, 23);// E

        int tileSet = PkWall.asPkTileSet(wall);

        // PkTileSet.count() est censé venir de ton étape 2
        assertEquals(1, PkTileSet.countOf(tileSet, TileKind.Colored.A));
        assertEquals(2, PkTileSet.countOf(tileSet, TileKind.Colored.B));
        assertEquals(3, PkTileSet.countOf(tileSet, TileKind.Colored.C));
        assertEquals(4, PkTileSet.countOf(tileSet, TileKind.Colored.D));
        assertEquals(5, PkTileSet.countOf(tileSet, TileKind.Colored.E));

        // On vérifie qu'il n'y a pas de First Player Marker par erreur
        assertEquals(0, PkTileSet.countOf(tileSet, TileKind.FIRST_PLAYER_MARKER));
    }
}
