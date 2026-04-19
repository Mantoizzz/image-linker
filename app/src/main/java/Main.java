import algorithm.CoreAlgorithm;
import entity.Tile;
import entity.TilePair;
import ui.GameController;
import ui.OverlayWindow;
import util.*;

import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final String WINDOW_TITLE = "Game Window";
    private static final int INTERVAL_MS = 500;

    public static void main(String[] args) throws Exception {
        double scale = ScreenshotHelper.getScaleFactor();
        Rectangle physicalBounds = ScreenshotHelper.getRectangle(WINDOW_TITLE);
        if (physicalBounds == null) {
            System.err.println("找不到游戏窗口，退出");
            return;
        }

        OverlayWindow[] ref = new OverlayWindow[1];
        SwingUtilities.invokeAndWait(() -> {
            ref[0] = new OverlayWindow();
            ref[0].show(physicalBounds, scale);
        });

        GameController controller = new GameController(ref[0]);

        while (true) {
            try {
                controller.updateQueue(solve());
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static List<TilePair> solve() {
        BufferedImage screenshot = ScreenshotHelper.captureWindow(WINDOW_TITLE, null);
        if (screenshot == null) return Collections.emptyList();

        BufferedImage board = Slicer.sliceToBoard(screenshot);
        List<Tile> tiles = Slicer.sliceToTiles(board);
        int[][] grid = BoardConverter.convert(tiles, Slicer.ROWS, Slicer.COLS);
        List<CoreAlgorithm.ConnectResult> results = new CoreAlgorithm().findAllConnectablePairs(grid);
        return TileMapper.map(results, tiles);
    }
}
