package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.GaodeAPIConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.EatWhatService;
import org.kookies.mirai.pojo.dto.PoiDTO;
import org.kookies.mirai.pojo.entity.api.gaode.request.AroundSearchRequestBody;
import org.kookies.mirai.pojo.entity.api.gaode.response.AddressResponse;
import org.kookies.mirai.pojo.entity.api.gaode.response.POI;
import org.kookies.mirai.pojo.entity.api.gaode.response.POIResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EatWhatServiceImpl implements EatWhatService {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private static final Random random = new Random();

    @Override
    public void eatWhat(long sender, Group group, String address, String city) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        if (Permission.checkPermission(sender, group.getId())) {
            AddressResponse addressResponse = getAddressResponse(address, city);

            AroundSearchRequestBody aroundSearchRequestBody = AroundSearchRequestBody.builder()
                    .location(addressResponse.getLocation())
                    .types(getPOI())
                    .radius(GaodeAPIConstant.RADIUS)
                    .sortrule(GaodeAPIConstant.SORT_RULE)
                    .build();

            POI poi = getPOI(aroundSearchRequestBody);

            sendMsg(at, group, chain, poi);
        }
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
        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
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
        int randomIdx = random.nextInt(pois.size());
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
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        // 将解析后的JsonObject对象转换为POIResponse对象
        return gson.fromJson(jsonObject, POIResponse.class);
    }


    /**
     * 获取一个随机的POI（兴趣点）类型。
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
            poiList = gson.fromJson(jsonArray, listType);
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
    public static int[] generateUniqueRandomNumbers(int n, int size) {
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
        JsonObject jsonObject = gson.fromJson(responseString, JsonObject.class);
        JsonArray geocodesArray = jsonObject.getAsJsonArray("geocodes");

        // 检查返回的地理编码数组是否为空
        if (geocodesArray.isEmpty()) {
            throw new RequestException(MsgConstant.INVALID_ADDRESS);
        }

        JsonObject firstGeocode = geocodesArray.get(0).getAsJsonObject();

        // 从第一个地理编码对象中解析出AddressResponse并返回
        return gson.fromJson(firstGeocode, AddressResponse.class);
    }

}
