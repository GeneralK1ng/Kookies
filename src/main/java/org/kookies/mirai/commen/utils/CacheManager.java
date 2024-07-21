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

    /**
     * 设置缓存方法，用于将发送者发送到群组的消息存储到缓存中。
     * <p>
     * 此方法首先检查消息是否为空，然后初始化群组目录，并更新个人消息缓存。
     * 如果在缓存更新过程中发生异常，将抛出CacheException异常。
     *
     * @param sender 消息发送者ID，用于标识消息的来源。
     * @param group 消息目标群组ID，用于标识消息的接收方。
     * @param message 消息内容，需要被存储到缓存中的文本信息。
     * @throws CacheException 如果缓存更新过程中发生异常，则抛出此异常。
     */
    public static void setCache(Long sender, Long group, String message) {
        try {
            // 检查消息是否为空，如果为空则直接返回，不进行缓存存储。
            if (message == null || message.isEmpty()) {
                return;
            }

            if (message.trim().startsWith("[不支持的消息")) {
                return;
            }

            // 初始化群组目录，确保后续缓存更新操作的文件目录正确。
            initDir(group);
            // 更新个人消息缓存，将消息存储到指定发送者和群组的缓存中。
            updatePersonalMessageCache(sender, group, message);
        } catch (Exception e) {
            // 如果在缓存操作过程中发生异常，抛出自定义的CacheException异常。
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
     * 根据群组ID获取当天单词计数文件。
     * <p>
     * 此方法用于生成当天的单词计数文件，文件名包含日期，以确保唯一性，并存储在指定群组的目录中。
     * 如果群组目录不存在，将会创建该目录。
     *
     * @param groupId 群组的唯一标识符。用于确定文件存储的目录。
     * @return 返回一个File对象，代表当天的单词计数文件。
     */
    public static File getTodayWordCountFile(Long groupId) {
        // 根据配置中启用的群组列表和群组ID，获取群组目录名
        String groupDirName = getGroupDirName(getConfig().getEnableGroupList(), groupId);
        File groupDir = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH, groupDirName);

        return new File(groupDir, LocalDate.now()+ ".txt");
    }

    /**
     * 获取昨天的单词计数文件。
     * <p>
     * 此方法用于根据群组ID生成并返回一个文件对象，该对象代表了昨天的单词计数文件。
     * 文件名格式为日期.txt，日期为昨天的日期。文件存储在特定群组的目录中。
     *
     * @param groupId 群组ID，用于确定文件存储的特定群组目录。
     * @return 昨天的单词计数文件的File对象。
     */
    public static File getYesterdayWordCountFile(Long groupId) {
        // 根据群组ID和启用的群组列表生成群组目录名
        String groupDirName = getGroupDirName(getConfig().getEnableGroupList(), groupId);
        // 创建群组目录的File对象
        File groupDir = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH, groupDirName);
        // 返回昨天的单词计数文件的File对象
        return new File(groupDir, LocalDate.now().minusDays(1) + ".txt");
    }

    /**
     * 根据群组ID获取一周单词统计文件。
     * <p>
     * 该方法用于创建并返回一个文件，该文件包含了指定群组一周内的单词计数。
     * 如果需要，会先生成相应的群组目录和临时统计文件。
     *
     * @param groupId 群组的唯一标识符，用于定位特定群组的单词统计文件。
     * @return 返回一个File对象，指向包含一周单词计数的临时文件。
     * @throws DataWriteException 如果写入文件过程中发生IO异常，则抛出此异常。
     */
    public static File getWeekWordCountFile(Long groupId) {
        // 根据群组ID和启用的群组列表生成群组目录名
        String groupDirName = getGroupDirName(getConfig().getEnableGroupList(), groupId);
        // 创建群组目录的File对象
        File groupDir = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH, groupDirName);

        // 在群组目录下生成一周的单词统计
        Map<String, Integer> weekCount = generateWeekCount(groupDir);

        // 创建临时文件，用于存储一周的单词计数
        File tempFile = new File(groupDir, "tempCnt.txt");

        try {
            // 将单词计数写入到临时文件中
            FileManager.writeWordMap2Txt(tempFile.getPath(), weekCount);
        } catch (IOException e) {
            // 如果写入文件发生IO异常，抛出自定义的数据写入异常
            throw new DataWriteException(MsgConstant.WEEK_WORD_COUNT_WRITE_ERROR);
        }

        // 返回临时文件File对象
        return tempFile;
    }

    /**
     * 根据群组ID获取该群组一周内各天的消息数量统计。
     * 该方法主要用于统计指定群组在一周内每天的消息数量，以便进行数据分析或展示。
     *
     * @param groupId 群组的唯一标识符，用于定位特定群组的消息数据。
     * @return 返回一个Map，其中键为一周内的日期（字符串格式），值为该日期对应的消息数量。
     */
    public static Map<String, Integer> getWeekCount(Long groupId) {
        // 根据群组ID和启用的群组列表生成群组目录名，用于定位群组的消息缓存目录。
        String groupDirName = getGroupDirName(getConfig().getEnableGroupList(), groupId);
        // 创建群组目录的File对象，用于后续访问或操作该群组的消息文件。
        File groupDir = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH, groupDirName);

        // 调用generateWeekCount方法，对群组目录下的消息文件进行统计，返回一周内每天的消息数量。
        return generateWeekCount(groupDir);
    }


    /**
     * 根据指定的目录生成过去七天的单词计数统计。
     * <p>
     * 该方法会检查过去七天每一天的单词统计文件，并将这些统计合并到一个单一的映射中。
     * 如果任何一天的统计文件不存在，将抛出DataLoadException异常。
     *
     * @param groupDir 字符串统计文件所在的目录。
     * @return 返回一个映射，其中键是单词，值是该单词在过去七天中的总出现次数。
     * @throws DataLoadException 如果任何一天的统计文件不存在，则抛出此异常。
     */
    private static Map<String, Integer> generateWeekCount(File groupDir) {
        // 初始化一个映射，用于存储过去七天的单词计数总和。
        Map<String, Integer> weekMap = new HashMap<>();
        // 遍历过去七天。
        for (int i = 0; i < 7; i++) {
            // 根据当前日期减去天数生成文件名，并构造文件对象。
            File wordCnt = new File(groupDir, LocalDate.now().minusDays(i) + ".txt");
            // 检查该日期的统计文件是否存在。
            if (wordCnt.exists()) {
                Map<String, Integer> tempMap;
                try {
                    // 读取该日期的统计文件内容到临时映射中。
                    tempMap = FileManager.readWordMap(wordCnt.getPath());
                } catch (IOException e) {
                    // 如果读取文件时发生IO异常，抛出自定义的数据加载异常。
                    throw new DataLoadException(MsgConstant.WEEK_WORD_COUNT_LOAD_ERROR);
                }
                // 将临时映射中的单词计数合并到weekMap中。
                tempMap.forEach((key, value) -> {
                    if (!key.trim().isEmpty()) {
                        if (weekMap.containsKey(key)) {
                            weekMap.put(key, weekMap.get(key) + value);
                        } else {
                            weekMap.put(key, value);
                        }
                    }
                });
            } else {
                // 如果有任何一天的统计文件不存在，抛出自定义的数据加载异常。
                throw new DataLoadException(MsgConstant.WEEK_WORD_COUNT_ISNT_HAS_SEVEN_DAYS);
            }
        }
        // 返回合并后的单词计数映射。
        return weekMap;
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
        String dirName = getGroupDirName(getConfig().getEnableGroupList(), group);
        // 创建群组目录的文件对象
        File groupDir = new File(MESSAGE_CACHE_DIR, dirName);

        File msgDir = new File(groupDir, DataPathInfo.PERSONAL_MSG_DIR);

        // 构建发送者文件名
        String senderFileName = sender + ".json";
        // 创建发送者文件对象
        return new File(msgDir, senderFileName);
    }


    /**
     * 处理个人消息文件已存在的情况。
     * 当个人消息文件已经存在时，本方法将决定是更新最新消息还是添加一条新消息。
     * 这取决于新消息的时间戳是否与文件中最新消息的时间戳相同。
     *
     * @param senderFile 消息发送者的文件，用于存储个人消息列表。
     * @param message 新的消息内容。
     */
    private static void handlePersonalMsgFileExists(File senderFile, String message) {
        // 从文件中加载现有的个人消息列表。
        List<PersonalMessage> personalMsgList = getPersonalMsgList(senderFile);
        // 获取个人消息列表中的最新消息。
        PersonalMessage latestMessage = personalMsgList.get(personalMsgList.size() - 1);
        // 统计新消息中的单词数量。
        Map<String, Integer> newWordCount = TextAnalyzer.countWords(message);

        // 检查最新消息是否是今天发送的。
        if (latestMessage.getDate().equals(LocalDate.now())) {
            // 如果是今天发送的，更新最新消息。
            updateLatestMessage(latestMessage, message, senderFile, newWordCount);
        } else {
            // 如果不是今天发送的，添加一条新消息。
            addNewPersonalMessage(personalMsgList, message, senderFile, newWordCount);
        }
        // 将更新后的个人消息列表写回文件。
        writePersonalMessageList(senderFile, personalMsgList);
    }

    /**
     * 更新最新的消息并更新发送者文件中的单词计数。
     * <p>
     * 此方法用于接收最新的消息，并将其添加到消息列表中。同时，它还会更新发送者文件中单词的计数，
     * 以确保单词频率的统计是最新的。这在处理文本消息并希望跟踪特定发送者使用的单词频率时非常有用。
     *
     * @param latestMessage 最新的个人信息，包含消息列表。此方法将新消息添加到此列表中。
     * @param message 新的消息文本，将被添加到最新的消息列表中。
     * @param senderFile 发送者文件，其中包含以前发送的消息和相应的单词计数。此文件将被更新以包含新消息的单词计数。
     * @param newWordCount 一个映射，用于存储新消息中的单词及其计数。此映射将与发送者文件中的现有单词计数合并。
     */
    private static void updateLatestMessage(PersonalMessage latestMessage, String message, File senderFile, Map<String, Integer> newWordCount) {
        latestMessage.getMessages().add(message);
        Map<String, Integer> existingWordCount = getWordMap(senderFile);

        newWordCount.forEach((word, count) -> {
            if (existingWordCount.containsKey(word)) {
                existingWordCount.put(word, existingWordCount.get(word) + count);
            } else {
                existingWordCount.put(word, count);
            }
        });

        writeWordMap(senderFile, existingWordCount);
    }

    /**
     * 添加新的个人消息到消息列表中，并更新发送者的单词计数。
     * <p>
     * 此方法用于在个人消息列表中添加一条新消息，并根据发送的消息更新发送者的单词计数。
     * 新消息的内容是固定的单条消息，日期为当前日期。
     *
     * @param personalMsgList 个人消息列表，新消息将被添加到这个列表中。
     * @param message 新消息的内容。
     * @param senderFile 发送者的信息文件，用于更新发送者的单词计数。
     * @param newWordCount 发送者新产生的单词计数，用于更新发送者的单词统计信息。
     */
    private static void addNewPersonalMessage(List<PersonalMessage> personalMsgList, String message, File senderFile, Map<String, Integer> newWordCount) {
        // 创建一个新的个人消息对象，包含消息内容和创建日期。
        PersonalMessage newMessage = PersonalMessage.builder()
                .messages(new LinkedList<>(Collections.singletonList(message)))
                .date(LocalDate.now())
                .build();

        // 将新消息添加到个人消息列表中。
        personalMsgList.add(newMessage);
        // 更新发送者的单词计数信息。
        writeWordMap(senderFile, newWordCount);
    }


    /**
     * 将单词计数映射写入到文件中。
     * <p>
     * 此方法封装了将单词计数映射写入到特定文件路径的过程。如果写入过程中发生IO异常，
     * 则会抛出一个自定义的数据写入异常。
     *
     * @param senderFile 源文件，用于计算目标文件路径。此文件不直接用于写入操作，
     *                   但其路径被用于生成单词计数映射的目标文件路径。
     * @param wordCount 单词计数映射，一个字符串到整数的映射，其中字符串是单词，
     *                  整数是该单词在文件中出现的次数。这个映射将被写入到文件中。
     * @throws DataWriteException 如果写入单词计数映射到文件时发生IO异常，则抛出此异常。
     */
    private static void writeWordMap(File senderFile, Map<String, Integer> wordCount) {
        try {
            // 将单词计数映射写入到由senderFile计算得到的目标文件路径中
            FileManager.writeWordMap2Txt(getWordMapFilePath(senderFile), wordCount);
        } catch (IOException e) {
            // 捕获到IO异常时，抛出一个自定义的数据写入异常
            throw new DataWriteException(MsgConstant.WORD_MAP_WRITE_ERROR);
        }
    }


    /**
     * 将个人消息列表写入到文件中。
     * <p>
     * 此方法用于将个人消息列表的JSON字符串写入到指定文件中，以供后续读取和使用。
     * 如果在写入过程中发生IO异常，将抛出一个自定义的数据写入异常。
     *
     * @param senderFile 指定的文件对象，个人消息列表将被写入到这个文件中。
     * @param personalMsgList 待写入的个人消息列表，列表中的每个元素都是一个个人消息对象。
     * @throws DataWriteException 如果写入过程中发生IO异常，则抛出此异常。
     */
    private static void writePersonalMessageList(File senderFile, List<PersonalMessage> personalMsgList) {
        try {
            // 使用GSON将个人消息列表转换为JSON树，然后将JSON树转换为字符串形式，最后写入到文件中。
            FileManager.write(senderFile.getPath(), GSON.toJsonTree(personalMsgList).getAsJsonArray().toString());
        } catch (IOException e) {
            // 如果在写入过程中发生IO异常，抛出自定义的异常。
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

        Map<String, Integer> wordCount = TextAnalyzer.countWords(message);

        writePersonalMessageList(senderFile, personalMessageList);
        writeWordMap(senderFile, wordCount);
    }

    /**
     * 根据给定的文件对象生成单词映射文件的路径。
     * <p>
     * 此方法用于确定存储单词频率统计的文件的位置和名称。
     * 它通过遍历给定文件的父目录结构，最终在最上层父目录中创建一个以当前日期命名的文本文件。
     *
     * @param senderFile 发送者文件，用于确定单词映射文件的相对位置。
     * @return 返回单词映射文件的完整路径。
     */
    private static String getWordMapFilePath(File senderFile) {
        // 获取发送者文件的父目录，即消息文件所在的目录
        File msgDir = new File(senderFile.getParent());
        // 获取消息目录的父目录，即群组目录
        File groupDir = new File(msgDir.getParent());
        // 以当前日期为名在群组目录中创建一个单词映射文件
        File wordMapFile = new File(groupDir, LocalDate.now() + ".txt");

        // 返回单词映射文件的完整路径
        return wordMapFile.getPath();
    }

    /**
     * 获取单词映射表。
     * <p>
     * 本方法旨在从指定的文件中加载单词映射表，该映射表用于后续的文本处理任务。
     *
     * @param senderFile 文件对象，表示要加载映射表的文件。
     * @return 返回一个Map，其中包含单词与对应出现次数的映射。
     * @throws DataLoadException 如果加载过程中发生IO异常，则抛出此异常。
     */
    private static Map<String, Integer> getWordMap(File senderFile) {
        // 根据发送者文件生成单词映射表文件的路径
        String wordMapFilePath = getWordMapFilePath(senderFile);
        try {
            // 从文件中读取并返回单词映射表
            return FileManager.readWordMap(wordMapFilePath);
        } catch (IOException e) {
            // 在发生IO异常时，抛出自定义的数据加载异常
            throw new DataLoadException(MsgConstant.WORD_MAP_LOAD_ERROR);
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
        File groupDir = new File(MESSAGE_CACHE_DIR, getGroupDirName(groups, group));
        if (!groupDir.exists()) {
            boolean created = groupDir.mkdirs();
            if (!created) {
                throw new DataLoadException(MsgConstant.MAKE_DIR_ERROR);
            }
        }

        File msgDir = new File(groupDir, DataPathInfo.PERSONAL_MSG_DIR);
        if (!msgDir.exists()) {
            boolean created = msgDir.mkdirs();
            if (!created) {
                throw new DataLoadException(MsgConstant.MAKE_DIR_ERROR);
            }
        }

        File wordCloudDir = new File(DataPathInfo.WORD_CLOUD_PATH);
        if (!wordCloudDir.exists()) {
            boolean created = wordCloudDir.mkdirs();
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

    /**
     * 从文件中加载个人消息列表。
     * <p>
     * 这个方法尝试从指定文件中读取JSON数组，并将其转换为个人消息的列表。
     * 如果文件读取失败，将抛出数据加载异常。
     *
     * @param senderFile 文件对象，表示要读取的JSON文件。
     * @return 返回一个个人消息的列表，列表中的每个元素都是一个PersonalMessage对象。
     * @throws DataLoadException 如果文件读取失败，则抛出此异常。
     */
    private static List<PersonalMessage> getPersonalMsgList(File senderFile) {
        JsonArray jsonArray;
        try {
            // 尝试读取文件中的JSON数组。
            jsonArray = FileManager.readJsonArray(senderFile.getPath());
        } catch (IOException e) {
            // 如果发生IO异常，抛出自定义的数据加载异常。
            throw new DataLoadException(MsgConstant.PERSONAL_MESSAGE_CACHE_LOAD_ERROR);
        }

        // 使用TypeToken来指定GSON解析的类型为List<PersonalMessage>。
        Type listType = new TypeToken<List<PersonalMessage>>() {}.getType();
        // 从JSON数组中解析出个人消息列表。
        List<PersonalMessage> personalMessageList = GSON.fromJson(jsonArray, listType);
        return personalMessageList;
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
    private static String getGroupDirName(List<Group> groups, Long groupId) {
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
