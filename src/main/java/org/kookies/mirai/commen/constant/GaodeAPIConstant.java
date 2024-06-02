package org.kookies.mirai.commen.constant;


import okhttp3.MediaType;

public class GaodeAPIConstant {
    public static final String AROUND_SEARCH_API_URL = "https://restapi.amap.com/v3/place/around";

    public static final String ADDRESS_GET_API_URL = "https://restapi.amap.com/v3/geocode/geo";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    public static final String SORT_RULE = "weight";

    public static final Integer OFFSET = 20;

    public static final String EXTENSIONS = "all";

    public static final Integer RADIUS = 3000;

    public static final String OUTPUT = "JSON";

    public static final Integer PAGE = 1;

    public static final String TARGET_CATEGORY = "餐饮服务";
}
