package org.kookies.mirai.commen.context;

import org.kookies.mirai.pojo.entity.Config;

public class ConfigContext {
    private static final ThreadLocal<Config> threadLocal = new ThreadLocal<>();

    public static void setConfig(Config config) {
        threadLocal.set(config);
    }

    public static Config getConfig() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}
