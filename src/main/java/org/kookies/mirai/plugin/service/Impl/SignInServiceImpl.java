package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.LiuLiApiConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.constant.WeiMengApiConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.DuplicatePermission;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.SignInService;
import org.kookies.mirai.pojo.dto.LuckDayDTO;
import org.kookies.mirai.pojo.entity.api.response.baidu.ai.ChatResponse;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author General_K1ng
 */
public class SignInServiceImpl implements SignInService {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private final Random random = new Random();

    /**
     * 为指定用户在指定群组中发送幸运日消息。
     *
     * @param sender 发送请求的用户ID
     * @param group  目标群组
     */
    @Override
    public void luckyDay(Long sender, Group group) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");


        if (Permission.checkPermission(sender, group.getId())) {
            if (DuplicatePermission.checkLuckyDayPermission(sender)) {
                LuckDayDTO luckDayDTO = LuckDayDTO.builder()
                        .sender(sender)
                        .romanceFortune(random.nextInt(100))
                        .schoolFortune(random.nextInt(100))
                        .wealthFortune(random.nextInt(100))
                        .build();
                List<Message> messages = createMessages(luckDayDTO);
                ChatResponse response = getResponse(messages, sender);

                sendMsg(at, group, chain, luckDayDTO, response.getResult());
            } else {
                sendMsg(at, group, chain, MsgConstant.LUCKY_DAY_PERMISSION_DUPLICATE_ERROR);
            }
        }
    }

    /**
     * 根据发送者和群组信息，发送今天的女友图片。
     * 此方法检查发送者是否有权限发送今天的女友图片，并且确保今天没有其他人发送过同样的图片。
     * 使用MiraiCode序列化和反序列化来处理AT发送者的信息。
     * 如果发送者有权限且今天获取女友图片次数还没有达到上限，则从数据库中获取图片并上传到群组中，最后发送消息。
     *
     * @param sender 发送者ID，用于检查权限和AT发送者。
     * @param group 群组对象，用于上传图片和发送消息。
     */
    @Override
    public void todayGirlFriend(long sender, Group group) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        // 检查发送者是否有权限发送图片
        if (Permission.checkPermission(sender, group.getId())) {
            // 检查今天发送者是否已经发送过女友图片
            if (DuplicatePermission.checkTodayGirlFriendPermission(sender)) {
                // 从数据库中获取今天的女友图片
                byte[] girlFriend = getTodayGirlFriend();
                // 将图片上传到群组
                Image image = group.uploadImage(ExternalResource.create(Objects.requireNonNull(girlFriend)));
                // 构建消息链，包含AT发送者和图片信息，最后发送消息
                sendMsg(at, group, chain, image);
            }else {
                byte[] budGuy = getBadGuy();
                Image image = group.uploadImage(ExternalResource.create(Objects.requireNonNull(budGuy)));
                sendMsg(at, group, chain, image);
            }
        }
    }

    /**
     * 摸鱼日报功能。
     * <p>
     * 该方法用于每日获取摸鱼日报图片，并将其上传至指定群组，然后向群组发送该图片。
     *
     * @param id 用户ID，用于权限检查。
     * @param group 目标群组，用于在其中上传和发送图片。
     * @throws RequestException 如果请求摸鱼日报图片时发生IO异常。
     * @assert 权限检查，确保用户有权进行此操作。
     */
    @Override
    public void messAroundDaily(long id, Group group) {
        assert Permission.checkPermission(id, group.getId());
        byte[] getMessAroundDaily;
        try {
            getMessAroundDaily = ApiRequester.getPhoto(WeiMengApiConstant.MESS_AROUND_DAILY);
        } catch (IOException e) {
            throw new RequestException(MsgConstant.MESS_AROUND_DAILY_GET_ERROR);
        }
        Image image = group.uploadImage(ExternalResource.create(getMessAroundDaily));
        group.sendMessage(image);
    }


    /**
     * 获取有问题的家伙的图像数据。
     * <p>
     * 本方法尝试通过调用ApiRequester的getBadGuyImg方法来获取特定图像数据，
     * 这些图像数据用于表示或标识“有问题的家伙”。
     * 如果在获取图像数据的过程中发生IO异常，方法将捕获该异常，
     * 并抛出一个自定义的RequestException异常，以通知调用者图像获取失败。
     *
     * @return byte[] 返回有问题家伙的图像数据，以字节数组形式表示。
     * @throws RequestException 如果获取图像数据过程中发生IO异常，则抛出此异常。
     */
    private byte[] getBadGuy() {
        try {
            // 尝试获取有问题的家伙的图像数据。
            return ApiRequester.getBadGuyImg();
        } catch (IOException e) {
            // 当发生IO异常时，抛出自定义异常。
            throw new RequestException(MsgConstant.IMAGE_GET_ERROR);
        }
    }


    /**
     * 发送消息到群组中。
     * 该方法用于向指定的群组发送一条文本消息和一张图片。首先，它将构建一个消息链，包含一个@
     * 符号和一条文本消息，然后将这个消息链和一张图片发送到群组中。
     *
     * @param at 消息中用于标识接收者的@符号。
     * @param group 目标群组。
     * @param chain 消息链构建器，用于构造待发送的消息。
     * @param image 待发送的图片。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, Image image) {
        // 将@符号添加到消息链中
        chain.add(at);
        // 在@符号后添加一个空格，以便与后续文本消息区分
        chain.append(" ");
        // 添加文本消息到消息链中，消息内容为召唤老婆的语句
        chain.append(new PlainText("正在召唤你今天的老婆..."));
        // 发送构建好的消息链到群组中
        group.sendMessage(chain.build());
        // 发送图片到群组中
        group.sendMessage(image);
    }

    /**
     * 发送消息给指定群组，关于指定用户的运势结果。
     *
     * @param at 指定接收消息的用户
     * @param group 发送消息的群组
     * @param chain 消息构建器，用于组装消息内容
     * @param luckDayDTO 运势数据传输对象，包含财运、桃花运和学业运等信息
     * @param response 个性化回复内容
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, LuckDayDTO luckDayDTO, String response) {
        // 将消息指向的用户添加到消息链中
        chain.add(at);

        // 添加一个空格，用于分隔用户名称和消息内容
        chain.append(" ");

        // 构建并添加运势消息内容，包括财运、桃花运、学业运以及个性化回复
        chain.append(new PlainText("你今天的运势结果来啦！\n"))
                .append(new PlainText("财运：" + luckDayDTO.getWealthFortune() + "%\n"))
                .append(new PlainText("桃花运：" + luckDayDTO.getRomanceFortune() + "%\n"))
                .append(new PlainText("学业：" + luckDayDTO.getSchoolFortune() + "%\n"))
                .append(new PlainText(response));

        // 发送构建完成的消息
        group.sendMessage(chain.build());
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
     * 获取今日女友图片。
     * 本方法尝试从指定的API地址获取一张动漫美少女图片，用于作为“今日女友”的展示图片。
     * 如果获取图片的过程中发生IO异常，将会抛出一个自定义的请求异常，提示图片获取失败。
     *
     * @return byte[] 返回一张图片的二进制数据。
     * @throws RequestException 如果发生IO异常，抛出此异常，携带错误信息。
     */
    private byte[] getTodayGirlFriend() {
        try {
            // 尝试通过API请求获取今日女友图片。
            return ApiRequester.getPhoto(LiuLiApiConstant.ANIME_URL);
        } catch (IOException e) {
            // 在发生IO异常时，抛出请求异常，携带预定义的错误消息。
            throw new RequestException(MsgConstant.IMAGE_GET_ERROR);
        }
    }


    /**
     * 根据提供的 LuckDayDTO 创建一系列消息对象。
     * <p>
     * 这个方法首先尝试从指定路径读取机器人信息，如果读取成功，将创建一个包含用户请求消息的新消息列表。
     * 用户请求消息包含了财运、学业运和桃花运的运势信息。
     * 如果读取机器人信息时发生IO异常，则会抛出运行时异常。
     * </p>
     *
     * @param luckDayDTO 幸运日数据传输对象，包含了财运、学业运和桃花运的信息。
     * @return 返回一个包含用户请求消息的消息列表。
     */
    private List<Message> createMessages(LuckDayDTO luckDayDTO) {
        List<Message> messages;
        try {
            // 尝试从指定路径读取机器人信息
            messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            // 如果读取过程中发生IO异常，抛出运行时异常
            throw new DataLoadException(MsgConstant.BOT_INFO_LOAD_ERROR);
        }
        // 创建一个消息对象，包含用户请求的内容和相应的运势信息
        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("请你帮我分析今天的运势并且给我一个可爱的祝福语，要多多表现Kookie的可爱与天真，\n" +
                        "所有的对话都要充满日常感,像人类的对话，不能让人感觉到跳脱。" +
                        "今天的运势是:" +
                        "财运：" + luckDayDTO.getWealthFortune() +
                        "学业：" + luckDayDTO.getSchoolFortune() +
                        "桃花运：" + luckDayDTO.getRomanceFortune())
                .build();
        // 将用户请求消息添加到消息列表中
        messages.add(message);

        return messages;
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
