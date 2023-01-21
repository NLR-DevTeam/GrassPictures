package cn.whitrayhb.grasspics.utils;

import java.util.Arrays;

public class ImageUtil {
    public static String getImageExt(byte[] imageBytes) {
        byte[] b = Arrays.copyOfRange(imageBytes, 0, 4);

        StringBuilder sb = new StringBuilder();
        for (byte ab : b) {
            sb.append(String.format("%02X", ab));
        }

        String type = sb.toString();

        if (type.contains("FFD8FF")) {
            return "jpg";
        } else if (type.contains("89504E47")) {
            return "png";
        } else if (type.contains("47494638")) {
            return "gif";
        } else if (type.contains("424D")) {
            return "bmp";
        } else {
            return "unknown";
        }
    }
}
