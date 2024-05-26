package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.CacheException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.pojo.dto.MessageCacheDTO;
import org.kookies.mirai.pojo.entity.MessageCache;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public static final File file = new File(DataPathInfo.MESSAGE_CACHE_PATH);

    public static MessageCache readCache(Long sender, String message) {
        try {
            // 尝试初始化并返回初始化结果
            return init(sender, message);
        } catch (Exception e) {
            // 如果初始化过程中发生异常，抛出授权异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
    }
    /**
     * 初始化消息缓存。
     * 如果缓存文件不存在，则创建新的缓存文件，并添加指定的消息。
     * 如果缓存文件已存在，则根据发送者和日期来更新或添加消息。
     *
     * @param sender 发送者ID，用于区分不同发送者的消息。
     * @param singleMessage 单个消息对象，需要被缓存的消息。
     * @return MessageCache 消息缓存对象，包含发送者和消息列表。
     */
    private static MessageCache init(Long sender, String singleMessage) {
        try {
            // 如果没有缓存文件
            if (!file.exists()) {
                // 建立对应文件夹
                file.getParentFile().mkdirs();

                // 新建消息链表
                List<String> messages = new ArrayList<>();
                messages.add(singleMessage);

                // 单个消息缓存对象，针对每个 sender
                MessageCache messageCache = MessageCache.builder()
                        .sender(sender)
                        .messages(messages)
                        .build();

                // 总缓存对象中的消息列表
                List<MessageCache> messageCaches = new ArrayList<>();
                messageCaches.add(messageCache);

                // 总缓存对象，针对每一天
                MessageCacheDTO messageCacheDTO = MessageCacheDTO.builder()
                        .date(LocalDate.now())
                        .messageCaches(messageCaches)
                        .build();

                // 转换为 Json 格式
                String json = gson.toJson(messageCacheDTO);

                // 写入
                FileManager.write(file.getPath(), json);

                return messageCache;
            // 如果已经有缓存文件
            } else {
                // 读取缓存，封装成一个json对象
                JsonObject jsonObject = FileManager.readJsonFile(file.getPath());

                //封装成总缓存对象
                MessageCacheDTO messageCacheDTO = gson.fromJson(jsonObject, MessageCacheDTO.class);

                // 查看是否是今天
                if (messageCacheDTO.getDate().equals(LocalDate.now())) {
                    MessageCache messageCache = findSender(sender, messageCacheDTO);
                    if (messageCache != null) {
                        messageCache.getMessages().add(singleMessage);
                        updateCache(messageCacheDTO);
                        FileManager.write(file.getPath(), gson.toJson(messageCacheDTO));
                        return messageCache;
                    } else {
                        List<String> messages = new ArrayList<>();
                        messages.add(singleMessage);

                        messageCache = MessageCache.builder()
                                .sender(sender)
                                .messages(messages)
                                .build();

                        messageCacheDTO.getMessageCaches().add(messageCache);
                        updateCache(messageCacheDTO);
                        String json = gson.toJson(messageCacheDTO);
                        FileManager.write(file.getPath(), json);
                        return messageCache;
                    }
                // 如果不是今天，则重写缓存
                } else {
                    List<String> messages = new ArrayList<>();
                    messages.add(singleMessage);

                    MessageCache messageCache = MessageCache.builder()
                            .sender(sender)
                            .messages(messages)
                            .build();

                    List<MessageCache> messageCaches = new ArrayList<>();
                    messageCaches.add(messageCache);

                    messageCacheDTO.setDate(LocalDate.now());
                    messageCacheDTO.setMessageCaches(messageCaches);

                    String json = gson.toJson(messageCacheDTO);
                    FileManager.write(file.getPath(), json);
                    return messageCache;
                }
            }
        } catch (IOException e) {
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
    }


    /**
     * 查找发送者对应的消息缓存。
     *
     * @param sender 发送者的ID，类型为Long。
     * @param dto 消息缓存的数据传输对象，包含了一个消息缓存列表，类型为MessageCacheDTO。
     * @return 如果找到匹配的发送者消息缓存，则返回该消息缓存对象；如果没有找到，则返回null。
     */
    private static MessageCache findSender(Long sender, MessageCacheDTO dto) {
        // 使用流对dto中的消息缓存进行遍历，并检查是否存在sender匹配的消息缓存
        return dto.getMessageCaches().stream()
                .filter(messageCache -> messageCache.getSender().equals(sender))
                .findFirst() // 找到第一个匹配项
                .orElse(null); // 如果没有找到匹配项，则返回null
    }

    /**
     * 更新消息缓存，只保留每个发送者的最近30条消息。
     *
     * @param messageCacheDTO 消息缓存的数据传输对象，包含了所有的消息缓存列表。
     */
    private static void updateCache(MessageCacheDTO messageCacheDTO) {
        for (MessageCache messageCache : messageCacheDTO.getMessageCaches()) {
            List<String> messages = messageCache.getMessages();
            if (messages.size() > 30) {
                // 保留最近的30条消息
                messageCache.setMessages(messages.subList(messages.size() - 30, messages.size()));
            }
        }
    }

}
