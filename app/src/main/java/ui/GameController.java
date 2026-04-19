package ui;

import entity.TilePair;
import util.PairSelector;
import util.Slicer;

import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameController {

    private static final int MAX_DISPLAY = 3;

    private final OverlayWindow overlay;

    private volatile List<TilePair> currentPairs = Collections.emptyList();
    private int skipFrames = 0;

    public GameController(OverlayWindow overlay) {
        this.overlay = overlay;
    }

    public void updateQueue(List<TilePair> newPairs) {
        if (skipFrames > 0) {
            skipFrames--;
            return;
        }

        if (currentPairs.isEmpty()) {
            refresh(newPairs);
            return;
        }

        if (isAnyCompleted(currentPairs, newPairs)) {
            skipFrames = 1;
            refresh(newPairs);
        }
    }

    private void refresh(List<TilePair> newPairs) {
        List<TilePair> selected = PairSelector.selectNonOverlapping(newPairs, MAX_DISPLAY);
        currentPairs = selected;
        SwingUtilities.invokeLater(() -> overlay.update(selected));
    }

    // 当前展示的任意一组，只要有一个端点从新列表消失，就认为已消除
    private boolean isAnyCompleted(List<TilePair> displayed, List<TilePair> newPairs) {
        return displayed.stream().anyMatch(pair -> isCompleted(pair, newPairs));
    }

    private boolean isCompleted(TilePair pair, List<TilePair> newPairs) {
        int rowA = pair.first().row(),  colA = pair.first().col();
        int rowB = pair.second().row(), colB = pair.second().col();
        boolean aExists = newPairs.stream().anyMatch(p ->
                (p.first().row()  == rowA && p.first().col()  == colA) ||
                (p.second().row() == rowA && p.second().col() == colA));
        boolean bExists = newPairs.stream().anyMatch(p ->
                (p.first().row()  == rowB && p.first().col()  == colB) ||
                (p.second().row() == rowB && p.second().col() == colB));
        return !aExists || !bExists;
    }
}
