package org.kookies.mirai.commen.exceptions;

import org.kookies.mirai.commen.exceptions.base.BaseException;

public class RequestException extends BaseException {
    public RequestException() {
    }
    public RequestException(String message) {
        super(message);
    }
}
