package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.CacheException;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.DataWriteException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.pojo.entity.Config;
import org.kookies.mirai.pojo.entity.Group;
import org.kookies.mirai.pojo.entity.PersonalMessage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author General_K1ng
 */
public class CacheManager {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    public static final File MESSAGE_CACHE_DIR = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH);

    public static final File CONFIG = new File(DataPathInfo.CONFIG_PATH);

    public static void setCache(Long sender, Long group, String message) {
        try {
            if (message == null || message.isEmpty()) {
                return;
            }

            initDir(group);
            updatePersonalMessageCache(sender, group, message);
        } catch (Exception e) {
            // 如果初始化过程中发生异常，抛出授权异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
    }

    /**
     * 获取指定发送者、群组和日期的个人消息缓存。
     *
     * @param sender 消息发送者ID
     * @param group 消息所属群组ID
     * @param date 指定的日期
     * @param size 返回消息的数量限制，如果为null或小于等于0，则不进行限制
     * @return 指定条件下的消息列表，如果无符合条件的消息或发生异常，则返回空列表
     * @throws CacheException 如果加载数据时发生异常
     */
    public static List<String> getPersonalMessageCache(Long sender, Long group, LocalDate date, Integer size) {
        try {
            // 根据发送者和群组获取个人消息文件
            File personalMsgFile = getPersonalMsgFile(sender, group);
            // 如果文件不存在，则抛出数据加载异常
            if (!personalMsgFile.exists()) {
                throw new DataLoadException(MsgConstant.PERSONAL_MESSAGE_CACHE_LOAD_ERROR);
            }

            // 加载个人消息列表
            List<PersonalMessage> personalMessages = getPersonalMsgList(personalMsgFile);

            // 筛选指定日期的消息，将消息内容合并为一个列表
            List<String> personalMessageList = personalMessages.stream()
                    .filter(personalMessage -> personalMessage.getDate().equals(date))
                    .map(PersonalMessage::getMessages)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // 如果指定了消息数量限制，则对结果进行截取
            if (size != null && size > 0) {
                return personalMessageList.stream()
                        .limit(size)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            // 如果发生任何异常，则抛出缓存异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
        // 如果没有符合条件的消息或发生异常，返回空列表
        return Collections.emptyList();
    }

    /**
     * 更新个人消息缓存。
     * <p>
     * 当发送者具有相应权限时，此方法用于更新指定群组中发送者的个人消息缓存。
     * 它首先根据群组ID判断是否应在缓存中创建或更新发送者的消息文件。
     * 如果文件不存在，将创建一个新的消息文件并添加消息；
     * 如果文件已存在，则直接更新文件中的消息内容。
     *
     * @param sender 发送者ID，用于标识消息的发送者。
     * @param group 群组ID，用于指定消息所属的群组。
     * @param message 消息内容，将被更新到发送者的个人消息缓存中。
     */
    private static void updatePersonalMessageCache(Long sender, Long group, String message) {
        // 检查发送者是否有权限在该群组中发送消息
        if (Permission.checkPermission(sender, group)) {
            File senderFile = getPersonalMsgFile(sender, group);

            // 如果发送者文件不存在，则创建新文件并添加消息
            if (!senderFile.exists()) {
                handleNewPersonalMsgFile(senderFile, message);
            } else {
                // 如果发送者文件已存在，则直接更新文件中的消息
                handlePersonalMsgFileExists(senderFile, message);
            }
        }
    }

    /**
     * 根据发送者和群组ID获取个人消息文件。
     * 这个方法用于确定并构造存储特定发送者在特定群组中发送的消息的文件路径。
     *
     * @param sender 发送者的ID。这是用来唯一标识消息发送者的。
     * @param group 群组的ID。这个参数指定了消息所属的群组。
     * @return 返回一个File对象，代表了特定发送者在特定群组中的消息文件。
     *         文件名基于发送者的ID，并以.json格式存储消息。
     */
    private static File getPersonalMsgFile(Long sender, Long group) {
        // 根据群组ID获取群组目录的名称
        String dirName = getDirName(getConfig().getEnableGroupList(), group);
        // 创建群组目录的文件对象
        File groupDir = new File(MESSAGE_CACHE_DIR, dirName);
        // 构建发送者文件名
        String senderFileName = sender + ".json";
        // 创建发送者文件对象
        return new File(groupDir, senderFileName);
    }


    /**
     * 处理个人消息文件，将新消息添加到文件中。
     * <p>
     * 该方法用于读取指定文件中的个人消息，将新消息添加到消息列表后，再将更新后的消息写回文件。
     * 如果在读取或写入文件过程中发生IO异常，将分别抛出DataLoadException和DataWriteException异常。
     *
     * @param senderFile 保存个人消息的文件，文件内容为JSON格式的个人消息对象。
     * @param message 新的消息内容，将被添加到个人消息列表中。
     * @throws DataWriteException 如果写入文件失败，则抛出此异常。
     */
    private static void handlePersonalMsgFileExists(File senderFile, String message) {

        List<PersonalMessage> personalMsgList = getPersonalMsgList(senderFile);

        PersonalMessage latestMessage = personalMsgList.get(personalMsgList.size() - 1);
        if (latestMessage.getDate().equals(LocalDate.now())) {
            latestMessage.getMessages().add(message);
        } else {
            personalMsgList.add(PersonalMessage.builder()
                    .messages(new LinkedList<>(Collections.singletonList(message)))
                    .date(LocalDate.now())
                    .build());
        }

        try {
            FileManager.write(senderFile.getPath(), GSON.toJsonTree(personalMsgList).toString());
        } catch (IOException e) {
            throw new DataWriteException(MsgConstant.PERSONAL_MESSAGE_CACHE_WRITE_ERROR);
        }
    }

    /**
     * 处理个人消息文件。
     * <p>
     * 将指定的消息存储为个人消息的JSON格式，写入到对应的个人消息文件中。
     *
     * @param senderFile 指定接收者的个人消息文件，用于写入消息。
     * @param message 待存储的消息内容。
     * @throws DataWriteException 如果写入文件时发生IO异常，则抛出此异常。
     */
    private static void handleNewPersonalMsgFile(File senderFile, String message) {
        // 初始化消息列表，用于存储待处理的消息。
        List<String> messages = new LinkedList<>();
        // 将传入的消息添加到消息列表中。
        messages.add(message);

        // 构建个人消息对象，包含消息列表和发送者信息。
        PersonalMessage personalMessage = PersonalMessage.builder()
                .messages(messages)
                .date(LocalDate.now())
                .build();

        List<PersonalMessage> personalMessageList = new ArrayList<>();
        personalMessageList.add(personalMessage);
        String jsonArray = GSON.toJsonTree(personalMessageList).getAsJsonArray().toString();

        try {
            // 将JSON字符串写入到指定的文件中。
            FileManager.write(senderFile.getPath(), jsonArray);
        } catch (IOException e) {
            // 如果写入文件时发生IO异常，抛出自定义的DataWriteException异常。
            throw new DataWriteException(MsgConstant.PERSONAL_MESSAGE_CACHE_WRITE_ERROR);
        }
    }

    /**
     * 初始化消息缓存目录
     * <p>
     * 如果消息缓存目录不存在，则尝试创建它。接着，为指定的群组创建一个子目录
     * 如果创建目录失败，将抛出DataLoadException异常
     *
     * @param group 需要初始化缓存目录的群组ID
     * @throws DataLoadException 如果创建目录失败，则抛出此异常
     */
    private static void initDir(Long group) {
        // 检查消息缓存目录是否存在，如果不存在，则尝试创建它
        if (!MESSAGE_CACHE_DIR.exists()) {
            boolean created = MESSAGE_CACHE_DIR.mkdirs();
            if (!created) {
                throw new DataLoadException(MsgConstant.MAKE_DIR_ERROR);
            }
        }
        // 获取配置中启用的群组列表
        List<Group> groups = getConfig().getEnableGroupList();
        // 根据群组列表和指定群组ID创建群组目录
        File groupDir = new File(MESSAGE_CACHE_DIR, getDirName(groups, group));

        // 检查群组目录是否存在，如果不存在，则尝试创建它
        if (!groupDir.exists()) {
            boolean created = groupDir.mkdirs();
            if (!created) {
                throw new DataLoadException(MsgConstant.MAKE_DIR_ERROR);
            }
        }
    }

    /**
     * 获取配置对象。
     * <p>
     * 该方法尝试从指定路径读取JSON配置文件，并将其解析为Config对象。如果读取或解析过程中发生异常，
     * 则抛出一个自定义的CacheException异常。
     *
     * @return Config 配置对象，包含应用所需的配置信息。
     * @throws CacheException 如果读取或解析配置文件失败，抛出此异常。
     */
    private static Config getConfig() {
        Config config;
        try {
            // 从指定路径读取JSON配置文件
            JsonObject jsonObject = FileManager.readJsonFile(CONFIG.getPath());
            // 使用GSON从JSON对象解析出Config对象
            config = GSON.fromJson(jsonObject, Config.class);
        } catch (Exception e) {
            // 抓住任何异常，并抛出自定义的CacheException异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
        return config;
    }

    private static List<PersonalMessage> getPersonalMsgList(File senderFile) {
        JsonArray jsonArray;
        try {
            jsonArray = FileManager.readJsonArray(senderFile.getPath());
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.PERSONAL_MESSAGE_CACHE_LOAD_ERROR);
        }

        Type listType = new TypeToken<List<PersonalMessage>>() {}.getType();
        return GSON.fromJson(jsonArray, listType);
    }


    /**
     * 根据群组列表和指定的群组ID，获取群组名称和标签的字符串表示。
     * <p>
     * 该方法首先通过群组ID在列表中找到对应的群组，然后将群组ID和群组的标签以特定格式拼接成字符串返回。
     * 如果指定的群组ID不存在于列表中，则返回仅包含群组ID的字符串。
     *
     * @param groups 群组列表，包含多个群组对象。
     * @param groupId 指定的群组ID，用于在群组列表中查找特定群组。
     * @return 返回拼接好的字符串，格式为"群组ID-标签1,标签2,..."，如果没有标签，则只有群组ID。
     */
    private static String getDirName(List<Group> groups, Long groupId) {
        StringBuilder sb = new StringBuilder();
        // 使用流式编程查找指定ID的群组，并处理找到的群组
        groups.stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .ifPresent(group -> {
            // 拼接群组ID
            sb.append(group.getId());
            // 获取群组的标签列表，过滤掉空标签，并用逗号拼接
            String tags = group.getTag().stream()
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.joining(","));
            // 拼接群组标签
            sb.append("-").append(tags);
        });
        // 返回拼接后的字符串
        return sb.toString();
    }


}
