package org.kookies.mirai.commen.constant;

import okhttp3.MediaType;

import java.io.Serializable;

public class VoiceApiConstant implements Serializable {

    // TODO 后续改成枚举，以支持其他语言
    public static final String PROMPT_LANG = "zh";

    public static final String TEXT_LANG = "zh";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final String DEFAULT_ROLE = "流萤";

}
