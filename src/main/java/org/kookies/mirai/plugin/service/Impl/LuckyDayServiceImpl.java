package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.DuplicatePermission;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.LuckyDayService;
import org.kookies.mirai.pojo.dto.LuckDayDTO;
import org.kookies.mirai.pojo.entity.ai.baidu.BaiduChatResponse;
import org.kookies.mirai.pojo.entity.ai.baidu.Message;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class LuckyDayServiceImpl implements LuckyDayService {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private Random random = new Random();

    /**
     * 为指定用户在指定群组中发送幸运日消息。
     * @param sender 发送请求的用户ID
     * @param group 目标群组
     */
    @Override
    public void luckyDay(Long sender, Group group) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");


        if (Permission.checkPermission(sender, group.getId())) {
            if (DuplicatePermission.checkPermission(sender)) {
                LuckDayDTO luckDayDTO = LuckDayDTO.builder()
                        .sender(sender)
                        .romanceFortune(random.nextInt(100))
                        .schoolFortune(random.nextInt(100))
                        .wealthFortune(random.nextInt(100))
                        .build();
                List<Message> messages = createMessages(luckDayDTO);
                BaiduChatResponse response = getResponse(messages);

                sendMsg(at, group, chain, response.getResult());
            } else {
                sendMsg(at, group, chain, MsgConstant.LUCKY_DAY_PERMISSION_DUPLICATE_ERROR);
            }
        }
    }

    /**
     * 向指定群组发送消息，消息内容包括@某对象和后续的文本内容。
     *
     * @param at 消息中被@的对象，通常是一个用户或用户组。
     * @param group 消息要发送到的目标群组。
     * @param chain 消息内容的构建器，用于拼接消息。
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

    private List<Message> createMessages(LuckDayDTO luckDayDTO) {
        List<Message> messages;
        try {
             messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("请你帮我分析今天的运势并且给我一个可爱的祝福语，今天的运势是" +
                        "财运：" + luckDayDTO.getWealthFortune() +
                        "学业：" + luckDayDTO.getSchoolFortune() +
                        "桃花运：" + luckDayDTO.getRomanceFortune())
                .build();
        messages.add(message);

        return messages;
    }



    /**
     * 根据传入的消息列表获取百度聊天机器人的响应结果。
     *
     * @param messages 用户向机器人发送的消息列表。
     * @return BaiduChatResponse 从百度API获取的聊天响应对象，包含具体的响应内容。
     * @throws RequestException 如果请求过程中发生IO异常，则抛出请求异常。
     */
    private BaiduChatResponse getResponse(List<Message> messages) {
        Response originalResponse = null;
        String json;
        try {
            // 向百度API发送请求并获取原始响应
            originalResponse = ApiRequester.sendBaiduRequest(messages);
            // 将原始响应的主体内容转换为字符串
            json = originalResponse.body().string();
        } catch (IOException e) {
            // 若发生IO异常，抛出自定义的请求异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }

        // 使用Gson将JSON字符串解析为BaiduChatResponse对象
        return gson.fromJson(json, BaiduChatResponse.class);
    }


}
