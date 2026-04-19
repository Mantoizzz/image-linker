package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class CoreAlgorithm {

    private static final int[][] DIRECTIONS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    /**
     * 找出棋盘上所有可通过最多两个折点连接的配对。
     * <p>
     * 约定：
     * 1. 0 表示空位，其他正整数表示图案编号。
     * 2. 只能连接值相同的两个图案。
     * 3. 路径只能水平或垂直移动，中间只能经过空位。
     * 4. 路径必须完全位于原始棋盘内部，不允许从棋盘外围绕线。
     *
     * @param grid 棋盘
     * @return 所有可以连接的配对及其路径关键点（起点、折点、终点）
     */
    public List<ConnectResult> findAllConnectablePairs(int[][] grid) {
        validateGrid(grid);
        if (grid.length == 0 || grid[0].length == 0) {
            return Collections.emptyList();
        }

        Map<Integer, List<Cell>> groupedCells = collectCells(grid);
        List<ConnectResult> results = new ArrayList<>();

        for (Map.Entry<Integer, List<Cell>> entry : groupedCells.entrySet()) {
            List<Cell> cells = entry.getValue();
            for (int i = 0; i < cells.size(); i++) {
                for (int j = i + 1; j < cells.size(); j++) {
                    Cell first = cells.get(i);
                    Cell second = cells.get(j);
                    List<Cell> path = findPath(grid, first, second);
                    if (path != null) {
                        results.add(new ConnectResult(
                                entry.getKey(),
                                first,
                                second,
                                path
                        ));
                    }
                }
            }
        }

        return results;
    }

    /**
     * 判断两个坐标是否可以连接，并返回一条可行路径。
     */
    public ConnectResult findConnectablePair(int[][] grid, int row1, int col1, int row2, int col2) {
        validateGrid(grid);
        validateCoordinate(grid, row1, col1);
        validateCoordinate(grid, row2, col2);

        if (row1 == row2 && col1 == col2) {
            return null;
        }

        int value = grid[row1][col1];
        if (value == 0 || value != grid[row2][col2]) {
            return null;
        }

        Cell first = new Cell(row1, col1);
        Cell second = new Cell(row2, col2);
        List<Cell> path = findPath(grid, first, second);
        if (path == null) {
            return null;
        }

        return new ConnectResult(value, first, second, path);
    }

    private void validateGrid(int[][] grid) {
        if (grid == null) {
            throw new IllegalArgumentException("grid cannot be null");
        }
        if (grid.length == 0) {
            return;
        }
        int cols = -1;
        for (int[] row : grid) {
            if (row == null) {
                throw new IllegalArgumentException("grid row cannot be null");
            }
            if (cols == -1) {
                cols = row.length;
            } else if (cols != row.length) {
                throw new IllegalArgumentException("grid must be rectangular");
            }
        }
    }

    private void validateCoordinate(int[][] grid, int row, int col) {
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
            throw new IllegalArgumentException("coordinate out of range");
        }
    }

    private Map<Integer, List<Cell>> collectCells(int[][] grid) {
        Map<Integer, List<Cell>> groupedCells = new HashMap<>();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int value = grid[row][col];
                if (value == 0) {
                    continue;
                }
                groupedCells.computeIfAbsent(value, key -> new ArrayList<>())
                        .add(new Cell(row, col));
            }
        }
        return groupedCells;
    }

    private List<Cell> findPath(int[][] grid, Cell start, Cell end) {
        if (clearLine(grid, start, end)) {
            return buildPath(start, end);
        }

        List<Cell> oneTurnPath = findOneTurnPath(grid, start, end);
        if (oneTurnPath != null) {
            return oneTurnPath;
        }

        for (Cell reachable : expandStraightReachable(grid, start, end)) {
            if (reachable.equals(end)) {
                return buildPath(start, end);
            }

            List<Cell> tailPath = findOneTurnOrDirectPath(grid, reachable, end);
            if (tailPath != null) {
                List<Cell> fullPath = new ArrayList<>();
                fullPath.add(start);
                fullPath.addAll(tailPath);
                return deduplicateConsecutive(fullPath);
            }
        }

        return null;
    }

    private List<Cell> findOneTurnOrDirectPath(int[][] grid, Cell start, Cell end) {
        if (clearLine(grid, start, end)) {
            return buildPath(start, end);
        }
        return findOneTurnPath(grid, start, end);
    }

    private List<Cell> findOneTurnPath(int[][] grid, Cell start, Cell end) {
        Cell corner1 = new Cell(start.row(), end.col());
        if (isEmptyTurnPoint(grid, corner1, start, end)
                && clearLine(grid, start, corner1)
                && clearLine(grid, corner1, end)) {
            return buildPath(start, corner1, end);
        }

        Cell corner2 = new Cell(end.row(), start.col());
        if (isEmptyTurnPoint(grid, corner2, start, end)
                && clearLine(grid, start, corner2)
                && clearLine(grid, corner2, end)) {
            return buildPath(start, corner2, end);
        }

        return null;
    }

    private boolean isEmptyTurnPoint(int[][] grid, Cell point, Cell start, Cell end) {
        return point.equals(start) || point.equals(end) || grid[point.row()][point.col()] == 0;
    }

    private List<Cell> expandStraightReachable(int[][] grid, Cell start, Cell end) {
        LinkedHashSet<Cell> reachable = new LinkedHashSet<>();
        for (int[] direction : DIRECTIONS) {
            int row = start.row() + direction[0];
            int col = start.col() + direction[1];
            while (isInside(grid, row, col)) {
                if (row == end.row() && col == end.col()) {
                    reachable.add(new Cell(row, col));
                    break;
                }
                if (grid[row][col] != 0) {
                    break;
                }
                reachable.add(new Cell(row, col));
                row += direction[0];
                col += direction[1];
            }
        }
        return new ArrayList<>(reachable);
    }

    private boolean clearLine(int[][] grid, Cell first, Cell second) {
        if (first.row() == second.row()) {
            int row = first.row();
            int fromCol = Math.min(first.col(), second.col()) + 1;
            int toCol = Math.max(first.col(), second.col());
            for (int col = fromCol; col < toCol; col++) {
                if (grid[row][col] != 0) {
                    return false;
                }
            }
            return true;
        }

        if (first.col() == second.col()) {
            int col = first.col();
            int fromRow = Math.min(first.row(), second.row()) + 1;
            int toRow = Math.max(first.row(), second.row());
            for (int row = fromRow; row < toRow; row++) {
                if (grid[row][col] != 0) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private boolean isInside(int[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    private List<Cell> buildPath(Cell... cells) {
        List<Cell> path = new ArrayList<>();
        Collections.addAll(path, cells);
        return deduplicateConsecutive(path);
    }

    private List<Cell> deduplicateConsecutive(List<Cell> path) {
        List<Cell> cleaned = new ArrayList<>();
        for (Cell cell : path) {
            if (cleaned.isEmpty() || !cleaned.get(cleaned.size() - 1).equals(cell)) {
                cleaned.add(cell);
            }
        }
        return cleaned;
    }

    public record ConnectResult(int pattern, Cell first, Cell second, List<Cell> path) {
        public ConnectResult(int pattern, Cell first, Cell second, List<Cell> path) {
            this.pattern = pattern;
            this.first = first;
            this.second = second;
            this.path = List.copyOf(path);
        }

        @Override
        public String toString() {
            return "ConnectResult{"
                    + "pattern=" + pattern
                    + ", first=" + first
                    + ", second=" + second
                    + ", path=" + path
                    + '}';
        }
    }

    public record Cell(int row, int col) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Cell cell)) {
                return false;
            }
            return row == cell.row && col == cell.col;
        }

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }
}