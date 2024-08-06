package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.GaodeAPIConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.ConvenienceService;
import org.kookies.mirai.pojo.dto.PoiDTO;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;
import org.kookies.mirai.pojo.entity.api.request.gaode.AroundSearchRequestBody;
import org.kookies.mirai.pojo.entity.api.response.baidu.ai.ChatResponse;
import org.kookies.mirai.pojo.entity.api.response.baidu.olympic.OlympicDataResponse;
import org.kookies.mirai.pojo.entity.api.response.gaode.AddressResponse;
import org.kookies.mirai.pojo.entity.api.response.gaode.POI;
import org.kookies.mirai.pojo.entity.api.response.gaode.POIResponse;
import org.kookies.mirai.pojo.entity.api.response.runoob.CodeRunResponse;


import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * @author General_K1ng
 */
public class ConvenienceServiceImpl implements ConvenienceService {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private static final Random RANDOM = new Random();

    /**
     * 根据提供的地址和城市信息，查询附近的一个点位（Point of Interest, POI），
     * 并向指定群组中的发送者发送关于该点位的消息。
     *
     * @param sender 发送请求的用户ID
     * @param group 目标群组
     * @param address 提供的地址信息
     * @param city 提供的城市信息
     */
    @Override
    public void eatWhat(long sender, Group group, String address, String city) {
        // 初始化消息链构建器
        MessageChainBuilder chain = new MessageChainBuilder();
        // 构建AT消息，指明消息是回复给特定用户的
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        // 检查发送者是否有权限执行操作
        if (Permission.checkPermission(sender, group.getId())) {
            // 获取地址验证响应
            AddressResponse addressResponse = getAddressResponse(address, city);

            // 构建用于附近搜索的请求体
            AroundSearchRequestBody aroundSearchRequestBody = AroundSearchRequestBody.builder()
                    .location(addressResponse.getLocation())
                    /* 获取搜索的点位类型 */
                    .types(getPOI())
                    /* 设置搜索半径 */
                    .radius(GaodeAPIConstant.RADIUS)
                    /* 设置排序规则 */
                    .sortrule(GaodeAPIConstant.SORT_RULE)
                    .build();

            // 根据请求体进行搜索，获取POI信息
            POI poi = getPOI(aroundSearchRequestBody);

            // 发送包含POI信息的消息
            sendMsg(at, group, chain, poi);
        }
    }

    /**
     * 执行代码运行操作。
     * <p>
     * 当收到执行代码的请求时，此方法将被调用。它首先检查发送者是否有权限执行代码，
     * 然后运行代码并准备回复消息。如果发送者有权限，它将构造消息并发送回群组。
     *
     * @param sender 发送请求的用户ID。用于标识请求的来源和可能的权限检查。
     * @param group  请求所在的群组。用于定位请求的上下文和发送回复。
     * @param code   用户提供的源代码。这是要执行的实际代码内容。
     * @param lang   代码的语言。用于指示如何处理和运行代码。
     */
    @Override
    public void codeRun(long sender, Group group, String code, String lang) {
        // 初始化消息链构建器
        MessageChainBuilder chain = new MessageChainBuilder();
        // 构建AT消息，指明消息是回复给特定用户的
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        if (Permission.checkPermission(sender, group.getId())) {
            CodeRunResponse codeRunResponse = getCodeRunResponse(code, lang);

            List<Message> botMsg = createBotMsg(codeRunResponse, code);
            ChatResponse chatResponse = getResponse(botMsg, sender);

            sendMsg(at, group, chain, chatResponse.getResult(), codeRunResponse);
        }

    }

    /**
     * 发送奥运日报
     * 本函数用于向指定群组发送奥运日报信息，包括奖牌榜和比赛结果等
     * 使用了权限校验来确保只有具有相应权限的用户才能触发此功能
     *
     * @param sender 发送者的QQ号，用于识别发送者并进行权限校验
     * @param group  发送消息的群组对象，用于确定消息发送的目标群组
     */
    @Override
    public void olympicDaily(long sender, Group group) {
        // 校验发送者是否有权限在目标群组中发送消息
        assert Permission.checkPermission(sender, group.getId());

        // 创建一个@消息，用于在消息中提及发送者
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        // 从API或数据源中获取最新的奥运数据
        List<OlympicDataResponse> data = getOlympicData();

        // 根据获取的奥运数据，创建机器人要发送的消息内容
        List<Message> botMsg = createBotMsg(data);

        // 根据机器人发送的消息内容，获取发送者可能的回应
        ChatResponse chatResponse = getResponse(botMsg, sender);

        // 向目标群组发送消息，包括回应结果和奥运数据
        sendMsg(group, chatResponse.getResult(), data);
    }


    /**
     * 获取奥运数据排名前10的记录
     * <p>
     * 本函数通过API请求获取奥运数据，解析JSON格式的响应，并提取出排名前10的奖牌信息
     *
     * @return 包含排名前10的奥运数据响应对象的列表
     * @throws RuntimeException 如果API请求失败或JSON解析出错，将抛出运行时异常
     */
    public static List<OlympicDataResponse> getOlympicData() {
        List<OlympicDataResponse> data;
        try {
            // 从API获取奥运数据
            Response olympicResponse = ApiRequester.getOlympicData();
            // 将响应体解析为JsonObject，以便提取所需数据
            JsonObject jsonObject = GSON.fromJson(olympicResponse.body().string(), JsonObject.class);

            // 逐步访问JSON对象，提取出奖牌列表的前10项
            JsonArray jsonArray = jsonObject.getAsJsonObject("tplData")
                    .getAsJsonObject("data")
                    .getAsJsonArray("tabsList")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("data")
                    .getAsJsonArray("medalList")
                    .get(0)
                    .getAsJsonArray();

            // 初始化计数器和新的JsonArray，用于存储前10项数据
            int cnt = 0;
            JsonArray newJsonArray = new JsonArray();
            for (JsonElement jsonElement : jsonArray) {
                if (cnt++ < 10) {
                    newJsonArray.add(jsonElement);
                }
            }

            // 定义类型令牌，用于GSON反序列化过程中的类型转换
            Type listType = new TypeToken<List<OlympicDataResponse>>() {}.getType();
            // 将筛选后的JSON数据反序列化为OlympicDataResponse对象列表
            data = GSON.fromJson(newJsonArray, listType);
        } catch (IOException e) {
            // 如果发生IO异常，包装为运行时异常并抛出
            throw new RuntimeException(e);
        }
        // 返回处理后的数据列表
        return data;
    }

    /**
     * 发送消息到群组，展示奖牌榜和相关聊天回复
     *
     * @param group 要发送消息的群组对象
     * @param chatRes 聊天机器人的回复文本
     * @param data 奥运会数据响应列表，包含各个国家的奖牌信息
     */
    private void sendMsg(Group group, String chatRes, List<OlympicDataResponse> data) {
        // 创建消息链构建器，用于构建要发送的消息链
        MessageChainBuilder chain = new MessageChainBuilder();

        // 创建字符串构建器，用于构建奖牌榜文本
        StringBuilder sb = new StringBuilder();
        // 添加奖牌榜标题
        sb.append("\t目前奖牌榜 \n");
        // 添加奖牌榜列名
        sb.append("排名\t国家\t金\t银\t铜\t总\n");
        // 遍历奥运会数据响应列表，构建每个国家的奖牌信息
        for (OlympicDataResponse country : data) {
            // 格式化国家的奖牌信息
            String msg = String.format("%d.\t%s\t%d\t%d\t%d\t%d\n",
                    country.getRank(),
                    country.getCountryName(),
                    country.getGold(),
                    country.getSilver(),
                    country.getBronze(),
                    country.getTotal());
            // 将国家的奖牌信息追加到字符串构建器中
            sb.append(msg);
        }
        // 发送构建好的奖牌榜文本消息到群组
        group.sendMessage(new PlainText(sb.toString()));
        // 发送聊天机器人的回复文本到群组
        group.sendMessage(new PlainText(chatRes));

    }


    /**
     * 发送消息给指定群组，包括代码执行结果和评价信息。
     *
     * @param at 被@的用户的消息链对象，用于指定消息的接收者。
     * @param group 目标群组对象，用于指定消息的发送地点。
     * @param chain 消息链构建器，用于构造发送的消息内容。
     * @param chatResponse 与代码执行无关的聊天回应内容。
     * @param codeRunResponse 代码执行响应对象，包含代码执行的结果和错误信息。
     * <p>
     * 此方法首先根据代码执行情况构建包含执行结果和错误信息的消息，
     * 然后发送该消息。接着，构建包含聊天回应内容的消息并发送。
     * 使用消息链构建器的目的是为了更灵活地构建消息内容。
     */
    private static void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String chatResponse, CodeRunResponse codeRunResponse) {
        chain.add(at);
        chain.append(" ");

        if (Objects.equals(codeRunResponse.getOutput(), "")) {
            chain.append(new PlainText("我靠你个byd写得代码真烂，直接报错啦！\n"));
            chain.append(new PlainText(codeRunResponse.getErrors()));
        } else {
            chain.append(new PlainText("你的这坨代码输出了这些： \n"));
            chain.append(new PlainText(codeRunResponse.getOutput()));
        }
        chain.append(new PlainText("别急，kookie还有点评呢"));
        group.sendMessage(chain.build());

        chain.clear();
        chain.add(at);
        chain.append(" ");
        chain.append(new PlainText(chatResponse));
        group.sendMessage(chain.build());
    }

    /**
     * 根据代码运行结果和代码内容创建机器人消息。
     * <p>
     * 此方法用于处理代码运行后的反馈信息，将用户的代码、代码的执行输出和错误信息
     * 组装成一条机器人消息，以便于后续的分析和建议生成。
     *
     * @param codeRunResponse 代码运行的结果，包含输出和错误信息。
     * @param code 用户提交的代码内容。
     * @return 包含机器人消息的列表，用于进一步处理和发送。
     * @throws DataLoadException 如果读取机器人信息失败，则抛出此异常。
     */
    private static List<Message> createBotMsg(CodeRunResponse codeRunResponse, String code) {
        List<Message> messages;
        try {
            // 尝试从指定路径读取机器人信息
            messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            // 如果读取过程中发生IO异常，抛出运行时异常
            throw new DataLoadException(MsgConstant.BOT_INFO_LOAD_ERROR);
        }

        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("请你可爱天真地分析这个代码，并且给出一些建议，用一段话说出。\n" +
                        "这里是代码：" + code + "\n" +
                        "这里是执行的输出：" + codeRunResponse.getOutput() + "\n" +
                        "这里是执行的error(如果存在的话)：" + codeRunResponse.getErrors())
                .build();
        messages.add(message);
        return messages;
    }

    /**
     * 根据奥运会数据创建机器人消息
     * <p>
     * 本函数首先尝试从文件中读取机器人信息，然后根据提供的奥运会数据生成一条新的消息，
     * 最后将这条消息添加到消息列表中返回
     *
     * @param data 奥运会数据响应列表，包含各个国家的奖牌信息
     * @return 消息列表，其中包括了根据奥运会数据生成的新消息
     * @throws DataLoadException 如果无法从文件中加载机器人信息，则抛出此异常
     */
    private static List<Message> createBotMsg(List<OlympicDataResponse> data) {
        List<Message> messages;
        try {
            // 从文件中读取机器人信息
            messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            // 如果读取失败，抛出自定义异常
            throw new DataLoadException(MsgConstant.BOT_INFO_LOAD_ERROR);
        }
        StringBuilder sb = new StringBuilder();

        // 遍历奥运会数据，构建奖牌榜信息
        for (OlympicDataResponse country : data) {
            String info = String.format("第%d名\t国家：%s\t金牌数：%d\t银牌数：%d\t铜牌数：%d\t总数：%d\n",
                    country.getRank(),
                    country.getCountryName(),
                    country.getGold(),
                    country.getSilver(),
                    country.getBronze(),
                    country.getTotal());
            sb.append(info);
        }

        // 使用构建的奖牌榜信息创建一条新消息，并添加到消息列表中
        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("这是目前正在进行的奥运会的奖牌榜，请你写一个简单的播报，用一段话说出。\n" +
                        sb.toString())
                .build();
        messages.add(message);
        return messages;
    }



    /**
     * 向指定群组发送消息，消息内容包括@某对象和后续的文本内容。
     *
     * @param at       消息中被@的对象，通常是一个用户或用户组。
     * @param group    消息要发送到的目标群组。
     * @param chain    消息内容的构建器，用于拼接消息。
     * @param poi      POI对象，包含详细的地点信息。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, POI poi) {
        // 将消息@对象添加到消息链中
        chain.add(at);
        // 在消息链后添加一个空格，为消息内容做分隔
        chain.append(" ");
        // 添加消息内容到消息链

        chain.append(new PlainText("来吃这个吧！\n"))
                .append(new PlainText(poi.getName() + "\n"))
                .append(new PlainText("这家店就位于：" + poi.getAddress() + "！\n "))
                .append(new PlainText("快去体验一下吧！"));

        byte[] img = getImage(poi);

        if (img != null) {
            chain.append(group.uploadImage(ExternalResource.create(Objects.requireNonNull(img))));
        } else {
            chain.append(new PlainText(MsgConstant.DONT_HAVE_IMAGE));
        }

        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
    }

    /**
     * 获取代码运行的响应。
     * <p>
     * 通过调用ApiRequester.getCodeRunResponse方法，向指定的API发送代码执行请求，
     * 并解析返回的响应，最终返回CodeRunResponse对象。
     *
     * @param code 要执行的代码字符串。
     * @param lang 代码的语言标识。
     * @return CodeRunResponse 对象，包含代码运行的结果。
     * @throws RuntimeException 如果在处理响应或解析JSON过程中发生IO异常，则抛出运行时异常。
     */
    private CodeRunResponse getCodeRunResponse(String code, String lang) {
        Response codeRunResponse;
        String json;
        try {
            codeRunResponse = ApiRequester.getCodeRunResponse(code, lang);
            json = codeRunResponse.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);

        return GSON.fromJson(jsonObject, CodeRunResponse.class);
    }


    /**
     * 根据给定的POI信息获取图片。
     *
     * @param poi 包含照片信息的POI对象，不能为null。
     * @return 如果成功获取到图片，返回图片的字节数组；如果POI没有照片或获取图片失败，则返回null。
     */
    private byte[] getImage(POI poi) {
        // 检查POI是否包含照片
        if (!poi.getPhotos().isEmpty()) {
            try {
                // 尝试从第一个照片的URL获取图片
                return ApiRequester.getPhoto(poi.getPhotos().get(0).getUrl());

            } catch (IOException e) {
                // 抓获获取图片过程中的IO异常，并转换为自定义的请求异常抛出
                throw new RequestException(MsgConstant.IMAGE_GET_ERROR);
            }
        }
        // 如果没有照片或获取失败，则返回null
        return null;
    }


    /**
     * 根据给定的搜索请求体获取一个随机的POI（Point of Interest）。
     *
     * @param aroundSearchRequestBody 搜索请求体，包含搜索的参数和条件。
     * @return 返回一个随机选择的POI对象。
     */
    private POI getPOI(AroundSearchRequestBody aroundSearchRequestBody) {
        // 获取基于搜索条件的POI响应
        POIResponse poiResponse = getPOIResponse(aroundSearchRequestBody);
        List<POI> pois = poiResponse.getPois();

        // 随机选择一个POI
        int randomIdx = RANDOM.nextInt(pois.size());
        return pois.get(randomIdx);
    }


    /**
     * 根据提供的AroundSearchRequestBody获取POI（兴趣点）的响应信息。
     *
     * @param aroundSearchRequestBody 包含围绕搜索请求的详细信息的请求体对象。
     * @return 返回一个包含POI信息的响应对象。
     * @throws RequestException 如果在发送请求或解析响应时发生IO异常，则抛出请求异常。
     */
    private POIResponse getPOIResponse(AroundSearchRequestBody aroundSearchRequestBody) {
        Response response; // 声明一个用于存储API响应的变量
        String json;
        try {
            // 发送围绕搜索的请求并获取响应
            response = ApiRequester.sendAroundSearchRequest(aroundSearchRequestBody);
            // 从响应对象中提取JSON字符串
            json = response.body().string();
        } catch (IOException e) {
            // 在出现IO异常时抛出请求异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }
        // 将JSON字符串解析为JsonObject对象
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);

        // 将解析后的JsonObject对象转换为POIResponse对象
        return GSON.fromJson(jsonObject, POIResponse.class);
    }


    /**
     * 获取一个随机的POI（兴趣点）类型。
     * <p>
     * 该方法从预先加载的POI列表中随机选择一个POI，并返回其类型。
     *
     * @return 返回随机选择的POI的新类型。
     * @throws DataLoadException 如果读取POI数据失败。
     */
    private String getPOI() {
        JsonArray jsonArray;
        List<PoiDTO> poiList;
        try {
            // 从指定路径读取JSON数组形式的POI数据
            jsonArray = FileManager.readJsonArray(DataPathInfo.EAT_WHAT_POI_PATH);
            // 使用TypeToken指定列表类型的泛型参数，以便反序列化
            Type listType = new TypeToken<List<PoiDTO>>() {}.getType();
            // 将JSON数组反序列化为PoiDTO列表
            poiList = GSON.fromJson(jsonArray, listType);
        } catch (IOException e) {
            // 若读取数据时发生异常，抛出数据加载异常
            throw new DataLoadException(MsgConstant.EAT_WHAT_POI_LOAD_ERROR);
        }

        int[] randomIdx = generateUniqueRandomNumbers(15, poiList.size());

        StringBuilder sb = new StringBuilder();
        for (int idx : randomIdx) {
            if (idx == randomIdx.length - 1) {
                sb.append(poiList.get(idx).getNewType());
            } else {
                sb.append(poiList.get(idx).getNewType()).append("|");
            }
        }
        return sb.toString();
    }

    /**
     * 生成指定数量的不重复随机数数组。
     *
     * @param n 需要生成的不重复随机数的个数。
     * @param size 随机数的范围大小，即随机数生成的上限为 size-1。
     * @return 一个包含 n 个不重复随机数的整型数组。
     */
    private static int[] generateUniqueRandomNumbers(int n, int size) {
        // 使用 HashSet 存储随机数，以保证元素的唯一性
        Set<Integer> numbers = new HashSet<>();
        // 创建 Random 对象用于生成随机数
        Random random = new Random();

        // 生成 n 个不重复的随机数
        while (numbers.size() < n) {
            int randomNumber = random.nextInt(size);
            numbers.add(randomNumber);
        }

        // 将 HashSet 中的元素转换为数组
        int[] result = new int[n];
        int i = 0;
        for (Integer num : numbers) {
            result[i++] = num;
        }

        return result;
    }

    /**
     * 根据提供的地址和城市获取地址响应。
     * <p>
     * 该方法通过调用API，传入地址和城市参数来获取地理编码信息，并将该信息封装成AddressResponse对象返回。
     * 如果请求失败或返回的地理编码数组为空，则抛出RequestException异常。
     *
     * @param address 需要查询的地址。
     * @param city 需要查询的城市。
     * @return AddressResponse 地址响应对象，包含地理编码信息。
     * @throws RequestException 如果请求失败或地址无效，则抛出此异常。
     */
    private AddressResponse getAddressResponse(String address, String city) {
        Response response;
        String responseString;
        try {
            // 向API发送地址请求
            response = ApiRequester.sendAddressRequest(address, city);
            responseString = response.body().string();
        } catch (IOException e) {
            // 处理请求过程中的IO异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }
        JsonObject jsonObject = GSON.fromJson(responseString, JsonObject.class);
        JsonArray geocodesArray = jsonObject.getAsJsonArray("geocodes");

        // 检查返回的地理编码数组是否为空
        if (geocodesArray.isEmpty()) {
            throw new RequestException(MsgConstant.INVALID_ADDRESS);
        }

        JsonObject firstGeocode = geocodesArray.get(0).getAsJsonObject();

        // 从第一个地理编码对象中解析出AddressResponse并返回
        return GSON.fromJson(firstGeocode, AddressResponse.class);
    }

    private ChatResponse getResponse(List<Message> messages, Long sender) {
        Response originalResponse;
        String json;
        try {
            // 向百度API发送请求并获取原始响应
            originalResponse = ApiRequester.sendBaiduRequest(messages, sender);
            // 将原始响应的主体内容转换为字符串
            json = originalResponse.body().string();
        } catch (IOException e) {
            // 若发生IO异常，抛出自定义的请求异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }

        // 使用Gson将JSON字符串解析为BaiduChatResponse对象
        return GSON.fromJson(json, ChatResponse.class);
    }
}
