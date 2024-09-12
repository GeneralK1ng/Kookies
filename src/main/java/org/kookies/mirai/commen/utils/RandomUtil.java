package org.kookies.mirai.commen.utils;

public class RandomUtil {
    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
