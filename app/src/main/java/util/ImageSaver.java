package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImageSaver {

    private static final String DEFAULT_DIR = "debug_images";

    public static void save(BufferedImage image, String fileName) {
        try {
            // 自动创建文件夹
            Files.createDirectories(Paths.get(DEFAULT_DIR));

            File outputFile = new File(DEFAULT_DIR, fileName);
            boolean success = ImageIO.write(image, "png", outputFile);

            if (success) {
                System.out.println("图片已保存: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("保存图片失败: " + e.getMessage());
        }
    }

    public static void saveWithTimestamp(BufferedImage image, String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        save(image, prefix + "_" + timestamp + ".png");
    }

    public static void saveTile(BufferedImage image, int row, int col) {
        // 创建 sub_tiles 文件夹专门放小块
        String tileDir = DEFAULT_DIR + "/tiles";
        try {
            Files.createDirectories(Paths.get(tileDir));
            File outputFile = new File(tileDir, String.format("tile_%d_%d.png", row, col));
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
