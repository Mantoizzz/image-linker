package util;

import algorithm.CoreAlgorithm.Cell;
import entity.TilePair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PairSelector {

    /**
     * 从所有可连通对中，贪心挑选路径互不重叠的子集。
     * 按路径长度升序优先，路径越短越简洁，不与已选路径共用格子才入选。
     */
    public static List<TilePair> selectNonOverlapping(List<TilePair> pairs, int maxCount) {
        List<TilePair> sorted = pairs.stream()
                .sorted(Comparator.comparingInt(p -> p.path().size()))
                .toList();

        List<TilePair> selected = new ArrayList<>();
        Set<Long> usedCells = new HashSet<>();

        for (TilePair pair : sorted) {
            if (selected.size() >= maxCount) break;

            List<Cell> path = pair.path();
            boolean hasOverlap = path.stream().anyMatch(c -> usedCells.contains(encode(c)));

            if (!hasOverlap) {
                selected.add(pair);
                path.forEach(c -> usedCells.add(encode(c)));
            }
        }

        return selected;
    }

    private static long encode(Cell cell) {
        return ((long) cell.row() << 32) | (cell.col() & 0xFFFFFFFFL);
    }
}
