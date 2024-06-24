package org.kookies.mirai.commen.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.DataWriteException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.entity.PersonalMessage;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author General_K1ng
 */
public class MessageCacheCleanerJob implements Job {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    /**
     * 执行作业的主体方法。该方法在作业调度时被调用，用于执行作业的具体逻辑。
     * 本作业的执行逻辑是清除旧的缓存。
     *
     * @param jobExecutionContext 作业执行上下文，包含作业执行所需的信息和状态。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        clearOldCache();
    }

    /**
     * 清理超过7天的旧缓存
     * <p>
     * 该方法定期检查并清除指定路径下的缓存文件，以保持缓存目录的清洁和效率
     * 它首先获取缓存目录的文件列表，然后遍历每个子目录，进一步清理其中的个人缓存文件
     * 清理的依据是文件的修改时间，如果文件的最后修改时间早于7天前，则认为是旧缓存并予以删除
     */
    private void clearOldCache() {
        // 创建缓存目录的文件对象
        File cacheDir = new File(DataPathInfo.MESSAGE_CACHE_DIR_PATH);
        // 计算7天前的日期
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        // 列出缓存目录下的所有子目录
        File[] groupDirs = cacheDir.listFiles();

        if (groupDirs != null) {
            for (File groupDir : groupDirs) {
                clearPersonalCaches(groupDir, sevenDaysAgo);
            }
        }
    }


    /**
     * 清理个人缓存文件中过期的消息。
     * <p>
     * 这个方法会遍历指定目录下的所有个人缓存文件，读取其中的消息列表，过滤掉那些日期早于指定日期的消息，
     * 然后将过滤后的消息列表写回缓存文件。
     *
     * @param groupDir 指定的缓存文件目录。这个目录下包含了所有个人缓存文件。
     * @param cutoffDate 过滤消息的日期阈值。任何早于这个日期的消息都会被过滤掉。
     */
    private void clearPersonalCaches(File groupDir, LocalDate cutoffDate) {
        // 列出groupDir下所有的个人缓存文件
        File msgDir = new File(groupDir, "msg");
        File[] personalCaches = msgDir.listFiles();
        // 如果personalCaches不为空，则遍历每个个人缓存文件
        if (personalCaches != null) {
            for (File personalCache : personalCaches) {
                // 读取个人缓存文件中的消息列表
                List<PersonalMessage> personalMsgList = readPersonalMessages(personalCache);
                // 如果消息列表不为空，则进行消息过滤
                if (personalMsgList != null) {
                    // 过滤掉早于cutoffDate的消息，得到过滤后的消息列表
                    List<PersonalMessage> filteredMsgList = filterMessages(personalMsgList, cutoffDate);
                    // 将过滤后的消息列表写回个人缓存文件
                    writeFilteredMessages(personalCache, filteredMsgList);
                }
            }
        }
    }

    /**
     * 从个人消息缓存文件中读取个人消息列表。
     * <p>
     * 此方法尝试解析缓存文件中的JSON数组为个人消息列表。如果文件读取失败，将抛出数据加载异常。
     *
     * @param personalCache 个人消息缓存文件，用于存储个人消息的JSON数组。
     * @return 返回解析后的个人消息列表。
     * @throws DataLoadException 如果读取或解析缓存文件时发生IO异常，则抛出此异常。
     */
    private List<PersonalMessage> readPersonalMessages(File personalCache) {
        try {
            JsonArray jsonArray = FileManager.readJsonArray(personalCache.getPath());
            Type listType = new TypeToken<List<PersonalMessage>>() {}.getType();
            return GSON.fromJson(jsonArray, listType);
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.PERSONAL_MESSAGE_CACHE_LOAD_ERROR);
        }
    }


    /**
     * 过滤个人消息列表，仅保留日期晚于或等于指定日期的消息。
     *
     * @param personalMsgList 消息列表，包含所有个人消息。
     * @param cutoffDate 用于过滤消息的日期阈值。
     * @return 过滤后的个人消息列表，仅包含日期晚于或等于阈值的消息。
     */
    private List<PersonalMessage> filterMessages(List<PersonalMessage> personalMsgList, LocalDate cutoffDate) {
        // 使用流式编程对消息列表进行过滤，只保留日期晚于或等于cutoffDate的消息
        return personalMsgList.stream()
                .filter(msg -> msg.getDate().isAfter(cutoffDate) || msg.getDate().isEqual(cutoffDate))
                .collect(Collectors.toList());
    }


    /**
     * 将过滤后的个人消息列表写入到本地缓存文件中。
     * 此方法封装了消息列表的JSON序列化和文件写入过程。如果写入文件过程中发生IO异常，
     * 将抛出一个自定义的数据写入异常。
     *
     * @param personalCache 本地缓存文件，用于存储过滤后的个人消息。
     * @param filteredMsgList 过滤后的个人消息列表，需要被写入到本地缓存中。
     * @throws DataWriteException 如果写入文件过程中发生IO异常，则抛出此异常。
     */
    private void writeFilteredMessages(File personalCache, List<PersonalMessage> filteredMsgList) {
        // 将过滤后的个人消息列表转换为JSON数组字符串
        String updatedJsonArray = GSON.toJsonTree(filteredMsgList).getAsJsonArray().toString();
        try {
            FileManager.write(personalCache.getPath(), updatedJsonArray);
        } catch (IOException e) {
            throw new DataWriteException(MsgConstant.PERSONAL_MESSAGE_CACHE_WRITE_ERROR);
        }
    }

}
