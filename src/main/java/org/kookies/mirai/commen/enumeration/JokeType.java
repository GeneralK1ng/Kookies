package org.kookies.mirai.commen.enumeration;

/**
 * @author General_K1ng
 */
public enum JokeType {
    SINGLE("single"),
    TWO_PART("twopart");

    private final String type;

    JokeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static JokeType fromType(String type) {
        for (JokeType jokeType : values()) {
            if (jokeType.getType().equalsIgnoreCase(type)) {
                return jokeType;
            }
        }
        throw new IllegalArgumentException("Unknown joke type: " + type);
    }
}
