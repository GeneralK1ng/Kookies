package org.kookies.mirai.commen.constant;

import java.io.Serializable;
import java.util.Random;


/**
 * @author General_K1ng
 */
public class WordCloudConstant implements Serializable {
    public static final int IMAGE_WIDTH = 1200;
    public static final int IMAGE_HEIGHT = 1200;
    public static final int PADDING = 2;
    public static final int BACKGROUND_RADIUS = 600;
    public static final int FONT_SCALAR_MIN = 32;
    public static final int FONT_SCALAR_MAX = 256;

    public static Random RANDOM = new Random();

    public static final String[] FONTS = new String[] {
            "楷体",
            "宋体",
            "华文楷体",
            "得意黑"
    };

    public static String randomFont() {
        int randomIndex = RANDOM.nextInt(FONTS.length);
        return FONTS[randomIndex];
    }
}
