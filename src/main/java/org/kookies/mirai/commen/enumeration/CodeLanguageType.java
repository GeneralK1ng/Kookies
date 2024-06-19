package org.kookies.mirai.commen.enumeration;

import lombok.Getter;

/**
 * @author General_K1ng
 */

@Getter
public enum CodeLanguageType {
    JAVA("java"),
    PYTHON("py3"),
    C("c"),
    CPP("cpp"),
    C_SHARP("cs"),
    PHP("php"),
    GO("go"),
    RUST("rs"),
    SWIFT("swift");

    final String language;

    CodeLanguageType(String language) {
        this.language = language;
    }

    public static String  getLanguageByName(String name) {
        for (CodeLanguageType value : values()) {
            if (value.language.equals(name)) {
                return value.language;
            }
        }
        return null;
    }
}
