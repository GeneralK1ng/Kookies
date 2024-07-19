package org.kookies.mirai.plugin.auth;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.AuthException;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.dto.BeautifulGirlPermissionDTO;
import org.kookies.mirai.pojo.dto.LuckDayPermissionDTO;
import org.kookies.mirai.pojo.dto.TodayGirlPermissionDTO;
import org.kookies.mirai.pojo.entity.Config;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * @author General_K1ng
 */
public class DuplicatePermission {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    private static final File LUCKY_DAY_FILE = new File(DataPathInfo.LUCKY_DAY_PERMISSION_PATH);

    private static final File TODAY_GIRL_FRIEND_FILE = new File(DataPathInfo.TODAY_GIRL_FRIEND_PERMISSION_PATH);

    private static final File BEAUTIFUL_GIRL_FILE = new File(DataPathInfo.BEAUTIFUL_GIRL_PERMISSION_PATH);
    /**
     * 检查发送者的今日运势权限。
     *
     * @param sender 发送者的ID，类型为Long。
     * @return 返回一个布尔值，表示权限检查的结果。如果权限检查通过，返回true；否则返回false。
     * @throws AuthException 如果在权限检查过程中发生异常，则抛出AuthException。
     */
    public static boolean checkLuckyDayPermission(Long sender) {
        try {
            // 尝试初始化并返回初始化结果
            return initLuckyDayPermission(sender);
        } catch (Exception e) {
            // 如果初始化过程中发生异常，抛出授权异常
            throw new AuthException(MsgConstant.LUCKY_DAY_PERMISSION_ERROR );
        }
    }

    /**
     * 检查今天是否具有向女友发送消息的权限。
     * 这个方法封装了权限检查的逻辑，旨在简化调用方的代码。
     * 它尝试初始化今天的女友权限状态，并在出现异常时抛出一个具体的授权异常。
     *
     * @param sender 发送者ID，用于权限检查的一部分。
     * @return 如果具有权限，则返回true；否则抛出AuthException异常。
     * @throws AuthException 如果权限初始化失败，将抛出此异常，携带错误消息。
     */
    public static boolean checkTodayGirlFriendPermission(long sender) {
        try {
            // 尝试初始化今天的女友权限状态。
            return initTodayGirlFriendPermission(sender);
        } catch (Exception e) {
            // 权限初始化异常时，抛出授权异常。
            throw new AuthException(MsgConstant.LUCKY_DAY_PERMISSION_ERROR );
        }
    }

    /**
     * 检查发送者是否具有美丽女孩权限。
     * <p>
     * 此方法尝试初始化发送者的美丽女孩权限。如果初始化成功，则表明发送者具有权限；
     * 如果初始化失败并捕获到异常，则表明存在权限初始化问题，具体可能是权限已存在或其他异常情况。
     *
     * @param sender 发送者ID，用于识别请求权限的实体。
     * @return 如果初始化成功，则返回true，表示发送者具有美丽女孩权限；如果初始化失败，则会抛出异常。
     * @throws AuthException 如果权限初始化失败，抛出此异常，异常信息指明了权限初始化的错误原因。
     */
    public static boolean checkBeautifulGirlPermission(long sender) {
        try {
            return initBeautifulGirlPermission(sender);
        } catch (Exception e) {
            throw new AuthException(MsgConstant.BEAUTIFUL_GIRL_DUPLICATE_PERMISSION_ERROR);
        }
    }

    /**
     * 初始化漂亮女孩权限列表。
     * <p>
     * 如果当天的权限文件不存在，则创建新的权限文件并返回初始化结果。
     * 如果当天的权限文件已存在，则检查发送者是否已存在于权限列表中。
     * 如果发送者不存在，则添加到列表中并返回true；否则返回false。
     *
     * @param sender 发送者ID，用于请求漂亮女孩权限。
     * @return 如果成功获取权限或已存在权限，则返回true；否则返回false。
     * @throws AuthException 如果读写文件发生IO异常，则抛出授权异常。
     */
    private static boolean initBeautifulGirlPermission(long sender) {
        try {
            if (!BEAUTIFUL_GIRL_FILE.exists()) {
                BEAUTIFUL_GIRL_FILE.getParentFile().mkdirs();
                return initBeautifulGirlSenderList(sender);
            } else {
                JsonObject jsonObject = FileManager.readJsonFile(BEAUTIFUL_GIRL_FILE.getPath());
                BeautifulGirlPermissionDTO dto = GSON.fromJson(jsonObject, BeautifulGirlPermissionDTO.class);

                if (dto.getDate().equals(LocalDate.now())) {
                    if (dto.getSenders().contains(sender)) {
                        return false;
                    } else {
                        dto.getSenders().add(sender);
                        String json = GSON.toJson(dto);
                        FileManager.write(BEAUTIFUL_GIRL_FILE.getPath(), json);
                        return true;
                    }
                } else {
                    return initBeautifulGirlSenderList(sender);
                }
            }
        } catch (IOException e) {
            throw new AuthException(MsgConstant.BEAUTIFUL_GIRL_DUPLICATE_PERMISSION_ERROR);
        }
    }

    /**
     * 初始化当天女朋友权限。
     * 检查当天是否已为发送者初始化权限，如果未初始化，则进行初始化。如果已初始化，检查发送者是否已达到最大权限次数。
     *
     * @param sender 发送者ID，用于标识请求权限的用户。
     * @return 如果初始化成功或发送者未达到最大权限次数，则返回true；如果发送者已达到最大权限次数，则返回false。
     * @throws AuthException 如果读写文件发生IO异常，则抛出授权异常。
     */
    private static boolean initTodayGirlFriendPermission(long sender) {
        try {
            if (!TODAY_GIRL_FRIEND_FILE.exists()) {
                TODAY_GIRL_FRIEND_FILE.getParentFile().mkdirs();
                return initTodayGirlSenderMap(sender);
            } else {
                JsonObject jsonObject = FileManager.readJsonFile(TODAY_GIRL_FRIEND_FILE.getPath());
                TodayGirlPermissionDTO dto = GSON.fromJson(jsonObject, TodayGirlPermissionDTO.class);

                if (dto.getDate().equals(LocalDate.now())) {
                    if (dto.getSenderWithTimes().containsKey(sender) && dto.getSenderWithTimes().get(sender) >= getConfig().getMaxTodayGirlTimes()) {

                        return false;
                    } else if (dto.getSenderWithTimes().containsKey(sender)) {
                        dto.getSenderWithTimes().put(sender, dto.getSenderWithTimes().get(sender) + 1);
                        String json = GSON.toJson(dto);
                        FileManager.write(TODAY_GIRL_FRIEND_FILE.getPath(), json);

                        return true;
                    } else {
                        dto.getSenderWithTimes().put(sender, 1);
                        String json = GSON.toJson(dto);
                        FileManager.write(TODAY_GIRL_FRIEND_FILE.getPath(), json);

                        return true;
                    }
                } else {
                    return initTodayGirlSenderMap(sender);
                }
            }
        } catch (IOException e) {
            throw new AuthException(MsgConstant.TODAY_GIRL_FRIEND_PERMISSION_DUPLICATE_ERROR);
        }
    }

    /**
     * 初始化发送者映射并保存到文件。
     * 此方法用于在系统中记录特定发送者的初始信息，将发送者与次数映射，并将此信息序列化为JSON格式，
     * 保存到指定的文件中。此方法始终返回true，表示初始化操作已执行。
     *
     * @param sender 发送者的ID，用于在映射中作为键。
     * @return 总是返回true，表示初始化过程完成。
     * @throws IOException 如果在写入文件过程中发生I/O错误。
     */
    private static boolean initTodayGirlSenderMap(long sender) throws IOException {
        // 创建一个TreeMap来有序地存储发送者ID和对应的发送次数。
        Map<Long, Integer> senderMap = new TreeMap<>();
        // 将当前发送者添加到映射中，初始发送次数为1。
        senderMap.put(sender, 1);

        // 构建TodayGirlPermissionDTO对象，包含当前日期和发送者映射。
        TodayGirlPermissionDTO todayGirlPermissionDTO = TodayGirlPermissionDTO.builder()
                .date(LocalDate.now())
                .senderWithTimes(senderMap)
                .build();

        // 将TodayGirlPermissionDTO对象序列化为JSON字符串。
        String json = GSON.toJson(todayGirlPermissionDTO);
        // 将JSON字符串写入指定的文件。
        FileManager.write(TODAY_GIRL_FRIEND_FILE.getPath(), json);
        // 返回true，表示初始化过程完成。
        return true;
    }

    /**
     * 初始化美丽女孩发送者列表。
     * <p>
     * 此方法用于将指定的发送者ID添加到美丽女孩的发送者列表中，并将更新后的列表保存到文件中。
     *
     * @param sender 发送者的ID，长整型。
     * @return 总是返回true，表示初始化操作已完成。
     * @throws IOException 如果在写入文件过程中发生IO异常。
     */
    private static boolean initBeautifulGirlSenderList(long sender) throws IOException{
        // 创建发送者列表，并将指定的发送者ID添加到列表中
        List<Long> senderList = new ArrayList<>();
        senderList.add(sender);

        // 构建一个包含当前日期和发送者列表的数据传输对象
        BeautifulGirlPermissionDTO dto = BeautifulGirlPermissionDTO.builder()
                .date(LocalDate.now())
                .senders(senderList)
                .build();

        // 将数据传输对象转换为JSON字符串
        String json = GSON.toJson(dto);
        // 将JSON字符串写入到指定的文件中
        FileManager.write(BEAUTIFUL_GIRL_FILE.getPath(), json);

        // 返回true，表示初始化操作已完成
        return true;
    }

    /**
     * 从配置文件路径中读取并解析配置文件，返回Config对象。
     * 这个方法封装了配置文件的读取和反序列化过程，对外提供了一个简单的接口来获取配置信息。
     * 如果在读取或解析过程中发生IO异常，将抛出一个自定义的数据加载异常。
     *
     * @return Config 返回解析后的Config对象，包含了应用程序所需的配置信息。
     * @throws DataLoadException 如果读取或解析配置文件失败，将抛出此异常。
     */
    private static Config getConfig() {
        JsonObject jsonObject;
        // 尝试读取并解析配置文件
        try {
            jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        } catch (IOException e) {
            // 在发生IO异常时，抛出一个更具体的自定义异常，通知调用者配置加载失败
            throw new DataLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
        // 使用GSON从JSON对象反序列化为Config对象
        return GSON.fromJson(jsonObject, Config.class);
    }


    /**
     * 初始化或更新幸运日权限文件。
     * 如果文件不存在，则创建新文件并添加发送者。
     * 如果文件存在，检查日期和发送者是否存在，若不存在则添加发送者。
     *
     * @param sender 发送者的ID，用于权限记录。
     * @return 返回true表示成功初始化或更新文件，返回false表示发送者已在当天的权限列表中。
     * @throws AuthException 如果读写文件发生IO异常，则抛出此异常。
     */
    private static boolean initLuckyDayPermission(Long sender) {
        try{
            if (!LUCKY_DAY_FILE.exists()) {
                LUCKY_DAY_FILE.getParentFile().mkdirs();
                List<Long> senderList = new ArrayList<>();
                senderList.add(sender);

                LuckDayPermissionDTO dto = LuckDayPermissionDTO.builder()
                        .date(LocalDate.now())
                        .sender(senderList)
                        .build();
                String json = GSON.toJson(dto);
                FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);

                return true;
            } else {
                JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.LUCKY_DAY_PERMISSION_PATH);
                LuckDayPermissionDTO dto = GSON.fromJson(jsonObject, LuckDayPermissionDTO.class);

                if (dto.getDate().isEqual(LocalDate.now())) {
                    if (dto.getSender().contains(sender)) {
                        return false;
                    } else {
                        dto.getSender().add(sender);

                        String json = GSON.toJson(dto);
                        FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);
                        return true;
                    }
                } else {
                    List<Long> senderList = new ArrayList<>();
                    senderList.add(sender);

                    dto.setDate(LocalDate.now());
                    dto.setSender(senderList);

                    String json = GSON.toJson(dto);
                    FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);

                    return true;
                }

            }
        } catch (IOException e) {
            throw new AuthException(MsgConstant.LUCKY_DAY_PERMISSION_ERROR);
        }
    }

}
