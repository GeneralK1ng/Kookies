package org.kookies.mirai.commen.constant;

import java.awt.*;
import java.io.Serializable;


/**
 * @author General_K1ng
 */
public class WordCloudConstant implements Serializable {
    public static final int IMAGE_WIDTH = 600;
    public static final int IMAGE_HEIGHT = 600;
    public static final int PADDING = 2;
    public static final int BACKGROUND_RADIUS = 300;
    public static final int FONT_SCALAR_MIN = 15;
    public static final int FONT_SCALAR_MAX = 120;
    public static final Color BACKGROUND_COLOR = new Color(0xEEEEEE);

    // TODO 需要一个颜色生成类生成随机组的颜色
    public static final Color[] COLOR_PALETTE = {
            new Color(0x6F63B6),
            new Color(0x4C3DB0),
            new Color(0x4931E3),
            new Color(0x402EE0)
    };
}
