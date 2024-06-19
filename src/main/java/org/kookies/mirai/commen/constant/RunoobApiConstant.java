package org.kookies.mirai.commen.constant;

import okhttp3.MediaType;

import java.io.Serializable;

/**
 * @author General_K1ng
 */
public class RunoobApiConstant implements Serializable {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    public static final String CODE_RUN_URL = "https://www.runoob.com/try/compile2.php";
}
