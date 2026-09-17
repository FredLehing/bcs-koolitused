package ee.bcskoolitus.infrastructure.util;

import java.nio.charset.StandardCharsets;

public class StringBytesConverter {

    public static byte[] stringToBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
