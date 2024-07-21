package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.*;
import org.kookies.mirai.commen.enumeration.BadGuyType;
import org.kookies.mirai.commen.enumeration.RequestType;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.pojo.entity.Config;
import org.kookies.mirai.pojo.entity.VoiceRole;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.ChatRequestBody;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;
import org.kookies.mirai.pojo.entity.api.request.gaode.AroundSearchRequestBody;
import org.kookies.mirai.pojo.entity.api.request.runoob.CodeRunRequestBody;
import org.kookies.mirai.pojo.entity.api.request.voice.VoiceRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author General_K1ng
 */
public class ApiRequester {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .connectTimeout(100000, TimeUnit.MILLISECONDS)
            .readTimeout(100000, TimeUnit.MILLISECONDS)
            .build();

    /**
     * 发送代码运行请求并返回响应。
     * <p>
     * 该方法通过调用一个外部API，将指定的代码片段以指定的编程语言运行，并返回运行结果。
     * 使用配置文件中的令牌进行身份验证，确保请求的安全性。
     *
     * @param code 要运行的代码片段。
     * @param lang 代码片段的编程语言。
     * @return 返回代码运行的响应。
     * @throws IOException 如果读取配置文件或发送网络请求时发生错误。
     */
    public static Response getCodeRunResponse(String code, String lang) throws IOException {
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = GSON.fromJson(jsonObject, Config.class);

        CodeRunRequestBody requestBody = CodeRunRequestBody.builder()
                .fileext(lang)
                .code(code)
                .token(config.getBotInfo().getRunoobToken())
                .build();

        String encodedCode = URLEncoder.encode(code, "UTF-8");

        requestBody.setCode(encodedCode);

        RequestBody body = RequestBody.create(RunoobApiConstant.FORM_MEDIA_TYPE,
                "code=" + requestBody.getCode() +
                        "&fileext=" + requestBody.getFileext() +
                        "&token=" + requestBody.getToken());


        Request request = new Request.Builder()
                .url(RunoobApiConstant.CODE_RUN_URL)
                .method(RequestType.POST.getMethod(), body)
                .addHeader("Content-Type", String.valueOf(RunoobApiConstant.FORM_MEDIA_TYPE))
                .build();

        return HTTP_CLIENT.newCall(request).execute();
    }

    /**
     * 获取美丽女孩视频的函数。
     * <p>
     * 通过发送HTTP GET请求到指定的API endpoint，来获取视频内容。
     *
     * @return byte[] - 返回从API获取的视频数据。
     * @throws IOException 如果网络请求失败或读取响应时发生错误。
     */
    public static byte[] getBeautifulGirlVideo() throws IOException {
        // 构建HTTP GET请求，指定请求的URL和请求类型。
        Request request = new Request.Builder()
                .url(LolimiApiConstant.BEAUTIFUL_GIRL_API)
                .method(RequestType.GET.getMethod(), null)
                .build();

        // 执行HTTP请求并获取响应。
        Response response = HTTP_CLIENT.newCall(request).execute();
        // 从响应中提取并返回视频数据。
        return response.body().bytes();
    }

    /**
     * 获取一个随机的坏人图片。
     * <p>
     * 该方法通过向百度图搜API发送请求，根据指定的坏人名称搜索图片，并随机选择一张返回。
     *
     * @return 包含随机坏人图片的字节数组。
     * @throws IOException 如果网络请求失败或解析响应出错。
     */
    public static byte[] getBadGuyImg() throws IOException {
        // 根据坏人类型获取坏人的名称
        String badGuyName = BadGuyType.getRandomBadGuyType().getBadGuyName();
        // 构建请求URL，包括坏人的名称和页码
        Request request = new Request.Builder()
                .url(LiuLiApiConstant.BAI_DU_IMG_SEARCH_URL +
                        "?msg=" + badGuyName +
                        "&pn=" + 1)
                // 设置请求方法为GET
                .method(RequestType.GET.getMethod(), null)
                .build();
        // 执行网络请求并获取响应
        Response response = HTTP_CLIENT.newCall(request).execute();
        // 使用GSON从响应体中解析出JSON对象
        JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
        // 从JSON对象中获取数据数组
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");
        // 随机选择一个图片URL
        Random random = new Random();
        String url = jsonArray.get(random.nextInt(jsonArray.size()))
                .getAsJsonObject()
                .get("thumbnailUrl")
                .toString()
                .replace("\"", "")
                .trim();
        // 根据URL获取图片字节数组并返回
        return getPhoto(url);
    }


    /**
     * 根据文本内容获取对应的语音数据。
     * <p>
     * 本函数通过读取配置文件，构建语音请求对象，并发送HTTP请求到语音API，以获取语音数据。
     * 具体流程包括：
     * 1. 读取配置文件，获取配置信息。
     * 2. 根据配置信息和输入的文本内容，构建语音请求对象。
     * 3. 将语音请求对象转换为JSON格式的请求体。
     * 4. 构建HTTP请求，并发送请求到语音API。
     * 5. 获取语音API的响应，并返回语音数据的字节流。
     *
     * @param content 文本内容，用于生成语音。
     * @param voiceRole 语音角色，用于生成语音。
     * @return 语音数据的字节流。
     * @throws IOException 如果读取配置文件或网络通信发生错误，则抛出此异常。
     */
    public static byte[] getVoiceWithText (String content, VoiceRole voiceRole) throws IOException {
        // 读取配置文件，获取配置信息
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = GSON.fromJson(jsonObject, Config.class);

        // 从配置信息中提取语音API相关的参数，并设置到语音请求对象中
        VoiceRequest voiceRequest = VoiceRequest.builder()
                .text(content)
                .prompt_lang(VoiceApiConstant.PROMPT_LANG)
                .text_lang(VoiceApiConstant.TEXT_LANG)
                .prompt_text(voiceRole.getPromptText())
                .gpt_weights_path(config.getBotInfo().getVoiceApiConfig().getGpt_weights_path() + "/" + voiceRole.getRole() + ".ckpt")
                .sovits_weights_path(config.getBotInfo().getVoiceApiConfig().getSovits_weights_path() + "/" + voiceRole.getRole() + ".pth")
                .ref_audio_path(config.getBotInfo().getVoiceApiConfig().getRef_audio_path() + "/" + voiceRole.getRole() + ".wav")
                .build();

        // 将设置完成的语音请求对象转换为JSON
        String json = GSON.toJson(voiceRequest);
        // 创建请求体，指定内容类型为JSON
        RequestBody requestBody = RequestBody.create(VoiceApiConstant.JSON_MEDIA_TYPE, json);

        // 构建HTTP请求，指定请求URL和方法，并设置请求体
        Request request = new Request.Builder()
                .url(config.getBotInfo().getVoiceApiConfig().getApiUrl())
                .method(RequestType.POST.getMethod(), requestBody)
                .build();

        // 发送HTTP请求，并获取响应
        Response response = HTTP_CLIENT.newCall(request).execute();

        // 返回语音数据的字节流
        return response.body().bytes();
    }

    /**
     * 发送周边搜索请求。
     * 该方法用于根据提供的周边搜索请求体，构建请求并发送至高德地图API，然后返回响应。
     * <p>
     * @param aroundSearchRequestBody 周边搜索请求体，包含搜索位置、类型、半径、排序规则等信息。
     * @return Response 返回从高德地图API获取的响应。
     * @throws IOException 如果在发送请求或读取响应时发生IO异常。
     */
    public static Response sendAroundSearchRequest (AroundSearchRequestBody aroundSearchRequestBody) throws IOException {
        // 从配置文件读取配置信息
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = GSON.fromJson(jsonObject, Config.class);

        Request request = new Request.Builder()
                .url(GaodeAPIConstant.AROUND_SEARCH_API_URL +
                        "?location=" + aroundSearchRequestBody.getLocation() +
                        "&types=" + aroundSearchRequestBody.getTypes() +
                        "&radius=" + aroundSearchRequestBody.getRadius() +
                        "&sortrule=" + aroundSearchRequestBody.getSortrule() +
                        "&offset=" + GaodeAPIConstant.OFFSET +
                        "&page=" + GaodeAPIConstant.PAGE +
                        "&extensions=" + GaodeAPIConstant.EXTENSIONS +
                        "&key=" + config.getBotInfo().getGaodeApiConfig().getApiKey())
                .method(RequestType.GET.getMethod(), null)
                .build();

        return HTTP_CLIENT.newCall(request).execute();
    }

    /**
     * 从指定的URL获取照片的字节数据。
     * <p>
     * @param photoUrl 照片的URL地址，不能为空。
     * @return 返回从指定URL获取到的照片的字节数据。
     * @throws IOException 如果在执行HTTP请求或读取响应时发生IO错误。
     */
    public static byte[] getPhoto(String photoUrl) throws IOException {
        // 构建一个GET请求到指定的URL
        Request request = new Request.Builder()
                .url(photoUrl)
                .method(RequestType.GET.getMethod(), null)
                .build();

        // 执行请求并获取响应
        Response response = HTTP_CLIENT.newCall(request).execute();
        // 从响应中提取并返回照片的字节数据
        return response.body().bytes();
    }


    /**
     * 发送地址查询请求到高德地图API。
     * <p>
     * @param address 查询的地址。
     * @param city 查询的城市。
     * @return 返回高德地图API的响应。
     * @throws IOException 如果读取配置文件或发送请求时发生IO异常。
     */
    public static Response sendAddressRequest(String address, String city) throws IOException{
        // 从配置文件读取配置信息
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = GSON.fromJson(jsonObject, Config.class);

        // 构建请求URL并创建请求对象
        Request request = new Request.Builder()
                .url(GaodeAPIConstant.ADDRESS_GET_API_URL +
                        "?address=" + address +
                        "&city=" + city +
                        "&output=" + GaodeAPIConstant.OUTPUT +
                        "&key=" + config.getBotInfo().getGaodeApiConfig().getApiKey())
                .method(RequestType.GET.getMethod(), null)
                .build();

        // 执行请求并返回响应
        return HTTP_CLIENT.newCall(request).execute();
    }


    /**
     * 向百度API发送请求。
     * <p>
     * @param messages 要发送的消息列表。
     * @param sender   发送者的QQ号。
     * @return 返回百度API的响应。
     * @throws IOException 如果执行HTTP请求时发生错误。
     */
    public static Response sendBaiduRequest(List<Message> messages, Long sender) throws IOException {
        // 创建请求体
        RequestBody body = createBaiduRequestBody(messages, sender);
        // 构建请求，包括设置URL、请求方法、请求头和请求体
        Request request = new Request.Builder()
                .url(BaiduApiConstant.API_URL +
                        "?access_token=" + getBaiduAccessToken())
                .method(RequestType.POST.getMethod(), body)
                .addHeader("Content-Type", String.valueOf(BaiduApiConstant.JSON_MEDIA_TYPE))
                .build();
        // 执行请求并返回响应

        return HTTP_CLIENT.newCall(request).execute();
    }

    /**
     * 创建百度聊天模型的请求体。
     * <p>
     * @param messages 要发送的消息列表，这些消息将被转换为百度聊天AI理解的格式。
     * @param sender   发送者的QQ号，用于设置用户ID。
     * @return 返回一个封装好的请求体对象，准备发送给百度AI聊天接口。
     */
    private static RequestBody createBaiduRequestBody(List<Message> messages, Long sender) {
        // 构建百度聊天请求体，设置消息、温度、top_k、top_p、惩罚分数、用户ID、是否流式返回等参数
        ChatRequestBody requestBody = ChatRequestBody.builder()
                .messages(messages)
                .temperature(0.9f)
                .top_p(1.0f)
                .penalty_score(1.4f)
                .stream(false)
                .user_id(String.valueOf(sender))
                .build();

        // 将构建好的请求体转换为JSON字符串
        String json = GSON.toJson(requestBody);

        // 创建并返回一个包含JSON字符串的请求体对象，用于向百度API发送请求
        return RequestBody.create(BaiduApiConstant.JSON_MEDIA_TYPE, json);
    }

    /**
     * 获取百度API的访问令牌（AccessToken）。
     * <p>
     * 该方法通过向百度API的授权端点发送POST请求，使用客户端的API密钥和密钥密码进行身份验证，
     * 以获取可用于API调用的访问令牌。
     * @return 返回从百度API授权服务器获取的访问令牌字符串。
     * @throws IOException 如果网络请求过程中发生IO异常。
     */
    private static String getBaiduAccessToken() throws IOException {
        JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        Config config = GSON.fromJson(jsonObject, Config.class);
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

    /**
     * 发起GET请求以获取一个暗黑笑话。
     * <p>
     * 这个方法通过调用HTTP客户端，向指定的API端点发送一个GET请求，以获取一个暗黑笑话。
     * 请求的URL由固定的API端点和一个类别参数组成，类别参数用于指定笑话的类型。
     *
     * @return Response 对象，包含从服务器接收到的响应。
     * @throws IOException 如果网络通信出现错误。
     */
    public static Response getDarkJoke() throws IOException {
        // 构建请求对象，指定请求的URL和方法类型
        Request request = new Request.Builder()
                .url(JokeApiConstant.DARK_JOKE_URL +
                        "/" + JokeApiConstant.CATEGORY)
                .method(RequestType.GET.getMethod(), null)
                .build();

        // 执行请求并返回响应
        return HTTP_CLIENT.newCall(request).execute();
    }

}
