package org.kookies.mirai.commen.exceptions;

import org.kookies.mirai.commen.exceptions.base.BaseException;

public class ConfigurationLoadException extends BaseException {
    public ConfigurationLoadException() {
    }
    public ConfigurationLoadException(String message) {
        super(message);
    }
}
