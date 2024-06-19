package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.CacheException;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.EvaluationService;
import org.kookies.mirai.pojo.dto.EvaluateSomebodyDTO;
import org.kookies.mirai.pojo.dto.MessageCacheDTO;
import org.kookies.mirai.pojo.entity.MessageCache;
import org.kookies.mirai.pojo.entity.api.response.baidu.ai.ChatResponse;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author General_K1ng
 */
public class EvaluationServiceImpl implements EvaluationService {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private static final File cache = new File(DataPathInfo.MESSAGE_CACHE_PATH);

    /**
     * 评估某人在群组中的信息并发送相关评价消息。
     * @param sender 发送请求的成员
     * @param group 目标群组
     * @param somebody 需要被评估的成员名称或ID
     */
    @Override
    public void evaluateSomebody(Member sender, Group group, String somebody) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender.getId() + "]");

        if (Permission.checkPermission(sender.getId(), group.getId())) {
            NormalMember member = getSomebody(group, somebody);

            EvaluateSomebodyDTO dto = EvaluateSomebodyDTO.builder()
                    .nameCard(member.getNameCard())
                    .nick(member.getNick())
                    .historyMsg(getHistoryMessage(member.getId()))
                    .build();

            List<Message> botMsg = createBotMsg(dto);

            ChatResponse response = getResponse(botMsg, sender.getId());
            sendMsg(at, group, chain, response.getResult());
        }

    }
    /**
     * 向指定群组发送消息，消息内容包括@某对象和后续的文本内容。
     *
     * @param at       消息中被@的对象，通常是一个用户或用户组。
     * @param group    消息要发送到的目标群组。
     * @param chain    消息内容的构建器，用于拼接消息。
     * @param response 要发送的消息内容。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String response) {
        // 将消息@对象添加到消息链中
        chain.add(at);
        // 在消息链后添加一个空格，为消息内容做分隔
        chain.append(" ");
        // 添加消息内容到消息链
        chain.append(new PlainText(response));
        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
    }

    /**
     * 创建机器人消息列表。
     * 此方法会首先尝试从指定路径读取机器人的信息，如果读取成功，将基于传入的DTO生成一条用户消息，并将其添加到机器人信息列表中。
     * 如果在读取机器人信息过程中发生IO异常，将抛出数据加载异常。
     *
     * @param dto 包含需要评价的人的信息的数据传输对象，包括nameCard、nick和历史消息。
     * @return 返回包含机器人信息和用户评价消息的列表。
     * @throws DataLoadException 如果读取机器人信息时发生IO异常。
     */
    private static List<Message> createBotMsg(EvaluateSomebodyDTO dto) {
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
                .content("请你可爱天真的说出对这个人的感受，他是一个什么样的人呢，你对他有什么感觉呢，表达你的情感：" +
                        " 这是他的名片:" + dto.getNameCard() +
                        " 这是他的昵称" + dto.getNick() +
                        " 这是他最近30条发言记录：" + dto.getHistoryMsg() +
                        " 说出感受，表达你的情感，说三段话")
                .build();
        messages.add(message);
        return messages;
    }


    /**
     * 获取指定用户的聊天历史消息。
     *
     * @param somebody 用户ID，表示要获取聊天历史的用户。
     * @return 返回该用户的一条聊天历史消息列表。如果找不到相关消息，返回null。
     */
    private static List<String> getHistoryMessage(Long somebody) {
        JsonObject jsonObject;

        try {
            // 尝试从JSON文件中读取聊天缓存数据
            jsonObject = FileManager.readJsonFile(DataPathInfo.MESSAGE_CACHE_PATH);
        } catch (IOException e) {
            // 如果读取失败，抛出缓存异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }

        // 将JSON对象转换为消息缓存数据传输对象
        MessageCacheDTO messageCacheDTO = gson.fromJson(jsonObject, MessageCacheDTO.class);

        // 通过流操作过滤出指定用户的聊天消息，并尝试获取消息链表
        return messageCacheDTO.getMessageCaches().stream()
                .filter(messageCache -> messageCache.getSender().equals(somebody))
                .map(MessageCache::getMessages)
                .findFirst()
                .orElse(null);
    }

    /**
     * 从指定群组中查找指定QQ号的成员。
     *
     * @param group 指定的群组对象，不可为null。
     * @param somebody 需要查找的成员的 MiraiCode，以字符串形式提供，不可为null或空。
     * @return 如果找到对应QQ号的成员，则返回该成员的NormalMember对象；如果没有找到或输入无效，则返回null。
     */
    private static NormalMember getSomebody(Group group, String somebody) {
        String regex = "\\[mirai:at:(\\d+)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(somebody);

        if (matcher.find()) {
            somebody = matcher.group(1);
        } else {
            throw new IllegalArgumentException(MsgConstant.INVALID_QQ_NUMBER);
        }

        // 将输入的字符串形式的QQ号转换为Long类型
        long somebodyQQ = Long.parseLong(somebody);
        // 通过流操作遍历群组中的所有成员，筛选出ID与输入QQ号匹配的成员，如果存在则返回第一个匹配的成员，否则返回null
        return group.getMembers().stream()
                .filter(member -> member.getId() == somebodyQQ)
                .findFirst().orElse(null);
    }

    /**
     * 根据传入的消息列表获取百度聊天机器人的响应结果。
     *
     * @param messages 用户向机器人发送的消息列表。
     * @param sender   发送请求的用户ID。
     * @return ChatResponse 从百度API获取的聊天响应对象，包含具体的响应内容。
     * @throws RequestException 如果请求过程中发生IO异常，则抛出请求异常。
     */
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
        return gson.fromJson(json, ChatResponse.class);
    }
}
