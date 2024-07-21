package org.kookies.mirai.commen.enumeration;

import lombok.Getter;

import java.util.Random;

/**
 * @author General_K1ng
 */

@Getter
public enum BadGuyType {
    DING_ZHEN("丁真"),
    DONG_XUE_LIAN("东雪莲"),
    GU_AI_LING("谷爱凌");

    private final String badGuyName;

    BadGuyType(String badGuyName) {
        this.badGuyName = badGuyName;
    }

    public static BadGuyType getRandomBadGuyType() {
        Random random = new Random();
        BadGuyType[] values = BadGuyType.values();
        return values[random.nextInt(values.length)];
    }
}
