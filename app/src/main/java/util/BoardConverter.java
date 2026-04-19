package util;

import entity.Tile;
import entity.TileHash;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BoardConverter {

    // 背景色 RGB(48, 76, 112)，容差 ±20
    private static final int BG_R = 48;
    private static final int BG_G = 76;
    private static final int BG_B = 112;
    private static final int BG_TOLERANCE = 20;

    // 同类牌匹配阈值（严格，防止形状相似的不同牌误判）
    private static final int TILE_THRESHOLD = 10;

    /**
     * 将切分好的 tile 列表转换为算法所需的 int[][] 棋盘。
     * 0 表示空格，正整数表示牌的类型 ID（相同 ID = 同类牌）。
     */
    public static int[][] convert(List<Tile> tiles, int rows, int cols) {
        int[][] grid = new int[rows][cols];
        List<TileHash> representatives = new ArrayList<>();

        for (Tile tile : tiles) {
            if (isEmpty(tile.image())) {
                grid[tile.row()][tile.col()] = 0;
                continue;
            }

            int matchedId = findMatch(tile.hash(), representatives);
            if (matchedId == -1) {
                representatives.add(tile.hash());
                matchedId = representatives.size(); // ID 从 1 开始
            }
            grid[tile.row()][tile.col()] = matchedId;
        }

        return grid;
    }

    // 采样 tile 中心 5×5 区域，全部接近背景色则判定为空格
    private static boolean isEmpty(BufferedImage image) {
        int cx = image.getWidth() / 2;
        int cy = image.getHeight() / 2;
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int rgb = image.getRGB(cx + dx, cy + dy);
                if (!isBackgroundColor(rgb)) return false;
            }
        }
        return true;
    }

    private static boolean isBackgroundColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Math.abs(r - BG_R) <= BG_TOLERANCE
                && Math.abs(g - BG_G) <= BG_TOLERANCE
                && Math.abs(b - BG_B) <= BG_TOLERANCE;
    }

    private static int findMatch(TileHash hash, List<TileHash> representatives) {
        for (int i = 0; i < representatives.size(); i++) {
            if (hash.hammingDistance(representatives.get(i)) < TILE_THRESHOLD) {
                return i + 1;
            }
        }
        return -1;
    }
}
