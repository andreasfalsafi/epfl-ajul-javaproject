package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

public sealed interface TileLocation {
    public record OffBoard(TileKind tileKind, int index) implements TileLocation{}

    public record OnSource(TileSource source, int index) implements TileLocation{}

    public record OnPattern(PlayerId playerId, TileDestination.Pattern line, int index) implements TileLocation{}

    public record OnWall(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color) implements TileLocation{}

    public record OnFloor(PlayerId playerId, int index) implements TileLocation{}


}
