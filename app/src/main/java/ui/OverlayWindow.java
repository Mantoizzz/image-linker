package ui;

import algorithm.CoreAlgorithm.Cell;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import driver.User32Ex;
import entity.TilePair;
import util.Slicer;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class OverlayWindow extends JWindow {

    private static final int GWL_EXSTYLE     = -20;
    private static final int WS_EX_LAYERED   = 0x00080000;
    private static final int WS_EX_TRANSPARENT = 0x00000020;

    private static final Color[] PAIR_COLORS = {
            new Color(255, 68,   68),  // 红
            new Color(255, 140,   0),  // 橙
            new Color(0,   204, 255),  // 青
    };

    private List<TilePair> pairs = Collections.emptyList();
    private double scaleFactor = 1.0;

    public OverlayWindow() {
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.SrcOver);
                if (!pairs.isEmpty()) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    drawPairs(g2);
                }
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        add(canvas);
    }

    public void show(Rectangle physicalBounds, double scaleFactor) {
        this.scaleFactor = scaleFactor;
        setBounds(toLogical(physicalBounds, scaleFactor));
        setVisible(true);
        applyClickThrough();
    }

    public void update(List<TilePair> newPairs) {
        this.pairs = newPairs;
        repaint();
    }

    private void applyClickThrough() {
        HWND hwnd = new HWND(Native.getComponentPointer(this));
        int exStyle = User32Ex.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        User32Ex.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, exStyle | WS_EX_LAYERED | WS_EX_TRANSPARENT);
    }

    private void drawPairs(Graphics2D g2) {
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pairs.size(); i++) {
            Color base = PAIR_COLORS[i % PAIR_COLORS.length];
            drawPair(g2, pairs.get(i), base);
        }
    }

    private void drawPair(Graphics2D g2, TilePair pair, Color base) {
        List<Cell> path = pair.path();

        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 200));
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = tileCenter(path.get(i));
            Point p2 = tileCenter(path.get(i + 1));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        drawDot(g2, tileCenter(path.get(0)), base);
        drawDot(g2, tileCenter(path.get(path.size() - 1)), base);
    }

    private Point tileCenter(Cell cell) {
        int x = Slicer.BOARD_X + cell.col() * (Slicer.TILE_WIDTH + Slicer.TILE_GAP) + Slicer.TILE_WIDTH / 2;
        int y = Slicer.BOARD_Y + cell.row() * (Slicer.TILE_HEIGHT + Slicer.TILE_GAP) + Slicer.TILE_HEIGHT / 2;
        return new Point(x, y);
    }

    private void drawDot(Graphics2D g2, Point center, Color color) {
        int r = 7;
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.WHITE);
        g2.drawOval(center.x - r, center.y - r, r * 2, r * 2);
        g2.setColor(color);
        g2.fillOval(center.x - r, center.y - r, r * 2, r * 2);
    }

    private static Rectangle toLogical(Rectangle physical, double scale) {
        return new Rectangle(
                (int) Math.round(physical.x / scale),
                (int) Math.round(physical.y / scale),
                (int) Math.round(physical.width / scale),
                (int) Math.round(physical.height / scale)
        );
    }
}
