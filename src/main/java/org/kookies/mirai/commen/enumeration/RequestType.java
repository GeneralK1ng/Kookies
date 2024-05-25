package org.kookies.mirai.commen.enumeration;

import lombok.Getter;

@Getter
public enum RequestType {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    OPTIONS("OPTIONS"),
    HEAD("HEAD");

    private final String method;

    // 构造函数
    RequestType(String method) {
        this.method = method;
    }

}
