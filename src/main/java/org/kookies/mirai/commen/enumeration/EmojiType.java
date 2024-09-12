package org.kookies.mirai.commen.enumeration;

import org.kookies.mirai.commen.utils.RandomUtil;

public enum EmojiType {
    ANIME("anime"),
    CHIIKAWA("chiikawa"),
    LONG("long"),
    CHESHIRE("cheshire");

    private final String emoji;

    EmojiType(String emoji) {
        this.emoji = emoji;
    }

    public static String randomEmoji() {
        return EmojiType.values()[RandomUtil.randomInt(0, EmojiType.values().length - 1)].emoji;
    }


}
