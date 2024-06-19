package org.kookies.mirai.commen.constant;

import okhttp3.MediaType;

/**
 * @author General_K1ng
 */
public class BaiduApiConstant {

    public static final String API_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat";

    public static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    public static final String GRANT_TYPE = "client_credentials";


}
