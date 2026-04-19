package entity;

import java.awt.image.BufferedImage;

public record Tile(
        int row,
        int col,
        int x,
        int y,
        int width,
        int height,
        BufferedImage image,
        TileHash hash
) {
}
