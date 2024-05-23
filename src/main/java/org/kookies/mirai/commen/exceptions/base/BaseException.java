package org.kookies.mirai.commen.exceptions.base;

public class BaseException extends RuntimeException{
    public BaseException() {
    }
    public BaseException(String message) {
        super(message);
    }
}
