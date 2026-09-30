package ee.bcskoolitus.infrastructure.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhotoNormalizerTest {

    // 1×1 lossless WebP
    private static final String WEBP_1X1 = "UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA==";

    @Test
    void normalize_landscapePng_returnsSquareJpegFromCenter() throws IOException {
        // 800×400: vasak ja parem veerand punane, keskmine pool sinine → ruut peab olema sinine
        BufferedImage image = new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 800; x++) {
            for (int y = 0; y < 400; y++) {
                image.setRGB(x, y, x >= 200 && x < 600 ? Color.BLUE.getRGB() : Color.RED.getRGB());
            }
        }

        byte[] normalizedPhoto = PhotoNormalizer.normalize(toBytes(image, "png"));

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(normalizedPhoto));
        assertEquals(PhotoNormalizer.SIZE, result.getWidth());
        assertEquals(PhotoNormalizer.SIZE, result.getHeight());
        assertEquals((byte) 0xFF, normalizedPhoto[0]);
        assertEquals((byte) 0xD8, normalizedPhoto[1]);
        Color corner = new Color(result.getRGB(5, 5));
        assertEquals(true, corner.getBlue() > 200 && corner.getRed() < 60);
    }

    @Test
    void normalize_webp_returnsJpeg() {
        byte[] normalizedPhoto = PhotoNormalizer.normalize(Base64.getDecoder().decode(WEBP_1X1));

        assertNotNull(normalizedPhoto);
        assertEquals((byte) 0xFF, normalizedPhoto[0]);
    }

    @Test
    void normalize_notAnImage_returnsNull() {
        assertNull(PhotoNormalizer.normalize("not an image".getBytes()));
    }

    private static byte[] toBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
