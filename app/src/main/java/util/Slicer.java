package util;

import entity.Tile;
import entity.TileHash;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Slicer {

    // 请根据目标窗口实际布局自行配置
    public static final int ROWS = 0;
    public static final int COLS = 0;

    public static final int BOARD_X = 0;
    public static final int BOARD_Y = 0;
    public static final int BOARD_WIDTH = 0;
    public static final int BOARD_HEIGHT = 0;

    public static final int TILE_WIDTH = 0;
    public static final int TILE_HEIGHT = 0;
    public static final int TILE_GAP = 0;

    public static BufferedImage sliceToBoard(BufferedImage image) {
        BufferedImage boardImage = image.getSubimage(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
        ImageSaver.save(boardImage, "board_offset4.png");
        return boardImage;
    }

    public static List<Tile> sliceToTiles(BufferedImage board) {
        validateBoard(board);
        List<Tile> tiles = new ArrayList<>(ROWS * COLS);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int startX = col * (TILE_WIDTH + TILE_GAP);
                int startY = row * (TILE_HEIGHT + TILE_GAP);
                BufferedImage tileImage = board.getSubimage(startX, startY, TILE_WIDTH, TILE_HEIGHT);
                TileHash hash = TileHasher.hash(tileImage);
                tiles.add(new Tile(
                        row,
                        col,
                        startX,
                        startY,
                        TILE_WIDTH,
                        TILE_HEIGHT,
                        tileImage,
                        hash
                ));
            }
        }
        return tiles;
    }

    private static void validateBoard(BufferedImage board) {
        if (board == null) {
            throw new IllegalArgumentException("board cannot be null");
        }
        if (board.getWidth() < BOARD_WIDTH || board.getHeight() < BOARD_HEIGHT) {
            throw new IllegalArgumentException(
                    "board is smaller than expected: "
                            + board.getWidth() + "x" + board.getHeight()
            );
        }
    }
}
