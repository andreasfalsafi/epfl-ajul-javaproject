package ch.epfl.ajul.mytests;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gui.TileLocation;
import ch.epfl.ajul.gui.Tiles;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TilesTest {

    // Petit mock rapide d'une partie à 2 joueurs
    private Game createGame2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void createGeneratesCorrectNumberOfAnchorsAndTilesFor2Players() {
        // NOTE: Si ce test plante avec "Toolkit not initialized", c'est que JavaFX refuse
        // de créer un Rectangle sans interface graphique sur ta machine. 
        Game game = createGame2P();
        Tiles tilesObj = Tiles.create(game);

        // --- 1. Vérification des Ancres ---
        // 2 joueurs = 5 fabriques (20) + centre (16) + 2 * plateaux (47) = 130 ancres !
        assertEquals(130, tilesObj.anchors().size(), "Il doit y avoir exactement 130 ancres pour une partie à 2 joueurs");

        // --- 2. Vérification des Tuiles ---
        // On doit avoir 20 tuiles de chaque couleur
        for (TileKind.Colored color : TileKind.Colored.values()) {
            List<Node> colorTiles = tilesObj.tiles().get(color);
            assertNotNull(colorTiles, "La liste de tuiles pour la couleur " + color + " est null");
            assertEquals(20, colorTiles.size(), "Il manque des tuiles pour la couleur " + color);

            // Vérification des classes de style CSS
            Node firstTile = colorTiles.get(0);
            assertTrue(firstTile.getStyleClass().contains("tile"), "La tuile doit avoir la classe 'tile'");
            assertTrue(firstTile.getStyleClass().contains(color.name()), "La tuile doit avoir la classe de sa couleur");
        }

        // Vérification du marqueur 1er joueur
        List<Node> markerTiles = tilesObj.tiles().get(TileKind.FIRST_PLAYER_MARKER);
        assertEquals(1, markerTiles.size(), "Il ne doit y avoir qu'un seul marqueur de 1er joueur");
        assertEquals("first-player-marker", markerTiles.get(0).getId(), "Le marqueur doit avoir le bon ID CSS");
    }

    @Test
    void locationAndSetLocationUseUserDataCorrectly() {
        Game game = createGame2P();
        Tiles tilesObj = Tiles.create(game);

        // On récupère une tuile au hasard (la première tuile A)
        Node tileNode = tilesObj.tiles().get(TileKind.Colored.A).get(0);

        // L'énoncé dit que `create` ne doit associer AUCUN emplacement aux tuiles (juste aux ancres)
        assertNull(Tiles.location(tileNode), "Les tuiles ne doivent pas avoir d'emplacement à la création");

        // On lui assigne un emplacement factice
        TileLocation mockLocation = new TileLocation.OffBoard(TileKind.Colored.A, 0);
        Tiles.setLocation(tileNode, mockLocation);

        // On vérifie qu'on arrive bien à le récupérer (via userData)
        assertEquals(mockLocation, Tiles.location(tileNode), "L'emplacement récupéré n'est pas celui qui a été assigné");
    }
}