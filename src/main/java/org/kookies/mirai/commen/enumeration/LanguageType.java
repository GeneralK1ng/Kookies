package org.kookies.mirai.commen.enumeration;

import lombok.Getter;

/**
 * @author General_K1ng
 */

@Getter
public enum LanguageType {
    CHINESE("zh"),
    ENGLISH("en"),
    JAPANESE("ja"),
    KOREAN("ko"),
    AUTO("auto");

    final String language;

    LanguageType(String language) {
        this.language = language;
    }
}
