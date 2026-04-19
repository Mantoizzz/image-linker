package util;

import algorithm.CoreAlgorithm.ConnectResult;
import entity.Tile;
import entity.TilePair;

import java.util.List;

public class TileMapper {

    public static List<TilePair> map(List<ConnectResult> results, List<Tile> tiles) {
        Tile[][] lookup = buildLookup(tiles);
        return results.stream()
                .map(result -> new TilePair(
                        lookup[result.first().row()][result.first().col()],
                        lookup[result.second().row()][result.second().col()],
                        result.path()
                ))
                .toList();
    }

    private static Tile[][] buildLookup(List<Tile> tiles) {
        int rows = tiles.stream().mapToInt(Tile::row).max().orElse(0) + 1;
        int cols = tiles.stream().mapToInt(Tile::col).max().orElse(0) + 1;
        Tile[][] lookup = new Tile[rows][cols];
        for (Tile tile : tiles) {
            lookup[tile.row()][tile.col()] = tile;
        }
        return lookup;
    }
}
