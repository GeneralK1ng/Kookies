package org.kookies.mirai.commen.constant;

import okhttp3.MediaType;

/**
 * @author General_K1ng
 */
public class BaiduApiConstant {

    public static final String AI_API_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat";

    public static final String AI_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    public static final String IMAGE_REQUEST_URL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/image-understanding/request?access_token=";

    public static final String IMAGE_RESULT_URL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/image-understanding/get-result?access_token=";

    public static final String IMAGE_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    public static final String GRANT_TYPE = "client_credentials";

    public static final String OLYMPIC_URL = "https://tiyu.baidu.com/al/major/home";


}
