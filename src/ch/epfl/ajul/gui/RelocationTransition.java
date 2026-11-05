package ch.epfl.ajul.gui;

import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import javafx.util.Duration;

/**
 * Représente une animation qui déplace un nœud JavaFX entre sa position actuelle
 * et une position cible donnée.
 *
 * <p>Cette classe hérite de {@link Transition} et calcule linéairement la position
 * intermédiaire du nœud à chaque fraction de temps de l'animation.
 *
 * @author Morgan Hauswirth (417840)
 * @author Andreas Falsafi (417792)
 */
public final class RelocationTransition extends Transition {
    private final Node node;
    private final Point2D pointStart;
    private final Point2D pointEnd;

    /**
     * Crée une nouvelle transition de déplacement pour un nœud donné.
     *
     * @param node     le nœud JavaFX à déplacer.
     * @param point    la position cible dans le système de coordonnées du parent.
     * @param duration la durée totale de l'animation.
     */
    public RelocationTransition(Node node, Point2D point, Duration duration) {
        this.node = node;
        this.pointStart = node.localToParent(Point2D.ZERO);
        this.pointEnd = point;
        setCycleDuration(duration);
    }

    /**
     * Calcule la position interpolée du nœud pour une fraction donnée de l'animation.
     *
     * @param frac la fraction du temps écoulé (entre 0.0 et 1.0).
     */
    @Override
    protected void interpolate(double frac) {

        Point2D currentPos = pointStart.interpolate(pointEnd, frac);
        node.relocate(currentPos.getX(), currentPos.getY());
    }
}
