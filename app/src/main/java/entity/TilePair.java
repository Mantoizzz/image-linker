package entity;

import algorithm.CoreAlgorithm.Cell;

import java.util.List;

public record TilePair(
        Tile first,
        Tile second,
        List<Cell> path
) {
}
