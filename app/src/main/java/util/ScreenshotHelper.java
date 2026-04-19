package util;

import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 截图工具类，处理 JDK 17 + Windows DPI 缩放问题。
 * <p>
 * 问题说明：
 * - DwmGetWindowAttribute 返回「物理像素」坐标（真实屏幕像素）
 * - JDK 9+ 的 Robot.createScreenCapture 接受「逻辑坐标」
 * - 150% DPI 时：逻辑坐标 = 物理坐标 / 1.5
 * 所以需要先将物理坐标转为逻辑坐标传给 Robot，
 * Robot 截出的图已经是完整的物理像素图，尺寸是逻辑尺寸 × 缩放比。
 * <p>
 * 如果截出的图尺寸是窗口的 1.5 倍，说明缩放比计算正确，图像内容也是正确的。
 */
public class ScreenshotHelper {

    /**
     * 获取系统 DPI 缩放比。
     * 150% DPI 返回 1.5，100% 返回 1.0。
     */
    public static double getScaleFactor() {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();
        System.out.printf("[ScreenshotHelper] DPI 缩放比: %.2f (%.0f%%)%n", scale, scale * 100);
        return scale;
    }

    /**
     * 根据物理像素坐标截图。
     * 内部自动将物理坐标转为逻辑坐标传给 Robot。
     *
     * @param physicalBounds DwmGetWindowAttribute 返回的物理像素矩形
     * @return 截图，失败返回 null
     */
    public static BufferedImage screenshot(Rectangle physicalBounds) {
        if (physicalBounds == null) {
            System.err.println("[ScreenshotHelper] physicalBounds 为 null");
            return null;
        }

        double scale = getScaleFactor();

        // 物理坐标 → 逻辑坐标
        Rectangle logicalBounds = new Rectangle(
                (int) Math.round(physicalBounds.x / scale),
                (int) Math.round(physicalBounds.y / scale),
                (int) Math.round(physicalBounds.width / scale),
                (int) Math.round(physicalBounds.height / scale)
        );

        System.out.printf("[ScreenshotHelper] 逻辑坐标: x=%d y=%d w=%d h=%d%n",
                logicalBounds.x, logicalBounds.y,
                logicalBounds.width, logicalBounds.height);

        try {
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(logicalBounds);
            System.out.printf("[ScreenshotHelper] 截图成功，图像尺寸: %dx%d%n",
                    image.getWidth(), image.getHeight());
            return image;
        } catch (AWTException e) {
            System.err.println("[ScreenshotHelper] Robot 初始化失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 截图并保存到文件。
     */
    public static boolean screenshotToFile(Rectangle physicalBounds, String outputPath) {
        BufferedImage image = screenshot(physicalBounds);
        if (image == null) return false;
        try {
            File output = new File(outputPath);
            ImageIO.write(image, "png", output);
            System.out.println("[ScreenshotHelper] 已保存: " + output.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[ScreenshotHelper] 保存失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 一步完成：置前窗口 → 等待渲染 → 截图 → 保存。
     *
     * @param windowTitle 窗口标题
     * @param outputPath  输出路径，传 null 则不保存文件
     * @return 截图，失败返回 null
     */
    public static BufferedImage captureWindow(String windowTitle, String outputPath) {
        Rectangle physicalBounds = getRectangle(windowTitle);
        if (physicalBounds == null) return null;

        // 4. 截图
        BufferedImage image = screenshot(physicalBounds);
        if (image == null) return null;

        // 5. 可选保存
        if (outputPath != null && !outputPath.isEmpty()) {
            screenshotToFile(physicalBounds, outputPath);
        }

        return image;
    }

    public static @Nullable Rectangle getRectangle(String windowTitle) {
        // 1. 置前窗口（核心步骤，窗口在底层时截图会截到别的窗口）
        var hwnd = WindowFinder.bringToFront(windowTitle);
        if (hwnd == null) return null;

        // 2. 等待窗口完成渲染，200ms 通常足够
        //    如果目标窗口有动画或加载过程，可以适当加大
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. 获取物理坐标
        return WindowFinder.getPhysicalBounds(hwnd);
    }
}
