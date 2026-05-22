package ch.epfl.ajul.gui;

import javafx.animation.Interpolatable;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import javafx.util.Duration;

public final class RelocationTransition extends Transition {
    Node node;
    Point2D pointStart;
    Point2D pointEnd;


    public RelocationTransition(Node node, Point2D point, Duration duration){
        this.node = node;
        this.pointStart = node.localToParent(Point2D.ZERO);
        this.pointEnd = point;
        setCycleDuration(duration);
    }
    @Override
    protected void interpolate(double frac) {

        Point2D oTW = pointStart.interpolate(pointEnd, frac);
        node.relocate(oTW.getX(), oTW.getY());
    }
}
