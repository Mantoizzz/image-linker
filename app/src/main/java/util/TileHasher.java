package util;

import entity.TileHash;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TileHasher {

    private static final int RESIZE = 32;
    private static final int HASH_SIZE = 8;

    public static TileHash hash(BufferedImage image) {
        BufferedImage resized = resize(image, RESIZE, RESIZE);
        long hashR = channelHash(resized, 0);
        long hashG = channelHash(resized, 1);
        long hashB = channelHash(resized, 2);
        return new TileHash(hashR, hashG, hashB);
    }

    private static BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return result;
    }

    // channel: 0=R, 1=G, 2=B
    private static long channelHash(BufferedImage image, int channel) {
        double[][] pixels = new double[RESIZE][RESIZE];
        for (int y = 0; y < RESIZE; y++) {
            for (int x = 0; x < RESIZE; x++) {
                int rgb = image.getRGB(x, y);
                pixels[y][x] = switch (channel) {
                    case 0 -> (rgb >> 16) & 0xFF;
                    case 1 -> (rgb >> 8) & 0xFF;
                    default -> rgb & 0xFF;
                };
            }
        }

        double[][] dct = dct2D(pixels);

        // flatten top-left 8×8 of DCT result
        double[] vals = new double[HASH_SIZE * HASH_SIZE];
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                vals[y * HASH_SIZE + x] = dct[y][x];
            }
        }

        // compute mean, excluding DC component at index 0
        double sum = 0;
        for (int i = 1; i < vals.length; i++) sum += vals[i];
        double mean = sum / (vals.length - 1);

        // generate 63-bit hash (bit 0..61 = vals[1..63] vs mean)
        long hash = 0;
        for (int i = 1; i < vals.length; i++) {
            if (vals[i] > mean) hash |= (1L << (i - 1));
        }
        return hash;
    }

    private static double[][] dct2D(double[][] pixels) {
        int n = pixels.length;
        double[][] temp = new double[n][n];
        double[][] result = new double[n][n];

        for (int y = 0; y < n; y++) {
            temp[y] = dct1D(pixels[y]);
        }
        for (int x = 0; x < n; x++) {
            double[] col = new double[n];
            for (int y = 0; y < n; y++) col[y] = temp[y][x];
            double[] dctCol = dct1D(col);
            for (int y = 0; y < n; y++) result[y][x] = dctCol[y];
        }
        return result;
    }

    private static double[] dct1D(double[] vals) {
        int n = vals.length;
        double[] result = new double[n];
        for (int k = 0; k < n; k++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += vals[i] * Math.cos(Math.PI * k * (2 * i + 1) / (2.0 * n));
            }
            result[k] = sum * (k == 0 ? Math.sqrt(1.0 / n) : Math.sqrt(2.0 / n));
        }
        return result;
    }
}
