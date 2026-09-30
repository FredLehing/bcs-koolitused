package ee.bcskoolitus.infrastructure.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

// Koolitaja pildi normaliseerimine üleslaadimisel: lõikab keskelt ruuduks, vähendab 400×400 px-ni
// ja kodeerib uuesti JPEG-iks. Uuesti kodeerimine eemaldab EXIF-metaandmed (nt GPS).
// PNG ja JPEG loeb ImageIO ise, WebP-d TwelveMonkeys imageio-webp pistik.
public class PhotoNormalizer {

    public static final String CONTENT_TYPE = "image/jpeg";
    public static final int SIZE = 400;
    private static final float JPEG_QUALITY = 0.85f;

    // null = baidid pole loetav pilt
    public static byte[] normalize(byte[] imageBytes) {
        BufferedImage image = readImage(imageBytes);
        if (image == null) {
            return null;
        }
        return writeJpeg(toSquare(image));
    }

    private static BufferedImage readImage(byte[] imageBytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException exception) {
            return null;
        }
    }

    // Keskelt ruut → SIZE×SIZE; läbipaistvus asendatakse valgega (JPEG-il alfakanalit pole)
    private static BufferedImage toSquare(BufferedImage image) {
        int side = Math.min(image.getWidth(), image.getHeight());
        int x = (image.getWidth() - side) / 2;
        int y = (image.getHeight() - side) / 2;
        BufferedImage square = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = square.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, SIZE, SIZE);
        graphics.drawImage(image, 0, 0, SIZE, SIZE, x, y, x + side, y + side, null);
        graphics.dispose();
        return square;
    }

    private static byte[] writeJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPEG_QUALITY);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } finally {
            writer.dispose();
        }
        return outputStream.toByteArray();
    }
}
