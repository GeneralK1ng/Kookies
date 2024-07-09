package org.kookies.mirai.pojo.entity.api.response.joke;

/**
 * @author General_K1ng
 */
public interface JokeResponse {
    boolean isError();
    String getCategory();
    String getType();
    String getLang();
}
