package ch.epfl.ajul;

/**
 * Représente un observateur de points.
 * Cet objet est informé chaque fois que les points d'un joueur changent.
 * Ses méthodes sont appelées par le jeu lorsque cela se produit.
 * @author morganhauswirth 417840
 * @author andreasfalsafi 417792
 */
public interface PointsObserver {

    /**
     * Appelée lorsque le joueur a ajouté une tuile à son mur et remporté des points.
     *
     * @param playerId l'identité du joueur
     * @param line     la ligne de motif/mur concernée
     * @param color    la couleur de la tuile ajoutée
     * @param points   le nombre de points remportés par ce placement
     */
    default void newWallTile(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color, int points) {}

    /**
     * Appelée à la fin d'une manche lorsqu'un joueur perd des points en raison
     * de la présence de tuiles sur sa ligne plancher.
     *
     * @param playerId l'identité du joueur
     * @param penalty  le nombre de points perdus (strictement positif)
     */
    default void floor(PlayerId playerId, int penalty) {}

    /**
     * Appelée à la fin de la partie lorsqu'un joueur gagne des points de bonus
     * car une ligne de son mur est complète.
     *
     * @param playerId l'identité du joueur
     * @param line     la ligne de motif/mur complétée
     * @param points   le nombre de points de bonus gagnés (généralement 2)
     */
    default void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {}

    /**
     * Appelée à la fin de la partie lorsqu'un joueur gagne des points de bonus
     * car une colonne de son mur est complète.
     *
     * @param playerId l'identité du joueur
     * @param column   l'index de la colonne complétée (entre 0 et 4 inclus)
     * @param points   le nombre de points de bonus gagnés (généralement 7)
     */
    default void fullColumn(PlayerId playerId, int column, int points) {}

    /**
     * Appelée à la fin de la partie lorsqu'un joueur gagne des points de bonus
     * car toutes les tuiles d'une couleur de son mur ont été placées.
     *
     * @param playerId l'identité du joueur
     * @param color    la couleur complétée
     * @param points   le nombre de points de bonus gagnés (généralement 10)
     */
    default void fullColor(PlayerId playerId, TileKind.Colored color, int points) {}

    /**
     * Un observateur de points vide dont aucune des méthodes ne fait quoi que ce soit.
     * Destiné à être utilisé principalement par l'intelligence artificielle.
     */
    PointsObserver EMPTY = new PointsObserver() {};
}