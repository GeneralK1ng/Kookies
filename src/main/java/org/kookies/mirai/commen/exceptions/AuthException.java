package org.kookies.mirai.commen.exceptions;

import org.kookies.mirai.commen.exceptions.base.BaseException;

public class AuthException extends BaseException {
    public AuthException() {
    }
    public AuthException(String message) {
        super(message);
    }
}
