package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.json.JSONObject;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.BaiduApiConstant;
import org.kookies.mirai.commen.context.ConfigContext;
import org.kookies.mirai.commen.enumeration.RequestType;

import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.pojo.entity.Config;
import org.kookies.mirai.pojo.entity.ai.baidu.BaiduChatRequestBody;
import org.kookies.mirai.pojo.entity.ai.baidu.Message;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


public class ApiRequester {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    /**
     * 向百度API发送请求。
     *
     * @param messages 要发送的消息列表。
     * @return 返回百度API的响应。
     * @throws IOException 如果执行HTTP请求时发生错误。
     */
    public static Response sendBaiduRequest(List<Message> messages) throws IOException {
        // 创建请求体
        RequestBody body = createBaiduRequestBody(messages);
        // 构建请求，包括设置URL、请求方法、请求头和请求体
        Request request = new Request.Builder()
                .url(BaiduApiConstant.API_URL +
                        "?access_token=" + getAccessToken())
                .method(RequestType.POST.getMethod(), body)
                .addHeader("Content-Type", String.valueOf(BaiduApiConstant.JSON_MEDIA_TYPE))
                .build();
        // 执行请求并返回响应
        Response response = HTTP_CLIENT.newCall(request).execute();

        return response;
    }


    /**
     * 创建百度聊天模型的请求体。
     *
     * @param messages 要发送的消息列表，这些消息将被转换为百度聊天AI理解的格式。
     * @return 返回一个封装好的请求体对象，准备发送给百度AI聊天接口。
     */
    private static RequestBody createBaiduRequestBody(List<Message> messages) {
        // 构建百度聊天请求体，设置消息、温度、top_k、top_p、惩罚分数、用户ID、是否流式返回等参数
        BaiduChatRequestBody requestBody = BaiduChatRequestBody.builder()
                .messages(messages)
                .temperature(0.85f)
                .top_k(1)
                .top_p(0.7f)
                .penalty_score(1.0f)
                .stream(false)
                .build();

        // 将构建好的请求体转换为JSON字符串
        String json = gson.toJson(requestBody);

        // 创建并返回一个包含JSON字符串的请求体对象，用于向百度API发送请求
        return RequestBody.create(BaiduApiConstant.JSON_MEDIA_TYPE, json);
    }
    /**
     * 获取百度API的访问令牌（AccessToken）。
     * 该方法通过向百度API的授权端点发送POST请求，使用客户端的API密钥和密钥密码进行身份验证，
     * 以获取可用于API调用的访问令牌。
     *
     * @return 返回从百度API授权服务器获取的访问令牌字符串。
     * @throws IOException 如果网络请求过程中发生IO异常。
     */
    private static String getAccessToken() throws IOException {
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = gson.fromJson(jsonObject, Config.class);
        // 构建请求体，包含授权类型、客户端ID和客户端密钥
        RequestBody body = RequestBody.create(BaiduApiConstant.FORM_MEDIA_TYPE,
                "grant_type=" + BaiduApiConstant.GRANT_TYPE +
                        "&client_id=" + config.getBotInfo().getBaiduApiConfig().getApiKey() +
                        "&client_secret=" + config.getBotInfo().getBaiduApiConfig().getSecretKey());

        // 创建请求对象，设置请求URL、请求方法、请求体和请求头
        Request request = new Request.Builder()
                .url(BaiduApiConstant.TOKEN_URL)
                .method(RequestType.POST.getMethod(), body)
                .addHeader("Content-Type", String.valueOf(BaiduApiConstant.FORM_MEDIA_TYPE))
                .build();

        // 执行请求并获取响应
        Response response = HTTP_CLIENT.newCall(request).execute();

        // 从响应体中解析出访问令牌并返回
        return new JSONObject(response.body().string()).getString("access_token");
    }

}
