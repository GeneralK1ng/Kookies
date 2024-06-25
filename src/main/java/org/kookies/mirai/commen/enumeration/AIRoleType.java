package org.kookies.mirai.commen.enumeration;

import lombok.Getter;

/**
 * @author General_K1ng
 */

@Getter
public enum AIRoleType {
    USER("user"),
    ASSISTANT("assistant");

    final String role;

    AIRoleType(String role) {
        this.role = role;
    }
}
