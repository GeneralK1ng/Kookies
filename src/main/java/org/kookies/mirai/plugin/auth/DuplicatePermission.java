package org.kookies.mirai.plugin.auth;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.AuthException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.dto.LuckDayPermissionDTO;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DuplicatePermission {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    public static final File file = new File(DataPathInfo.LUCKY_DAY_PERMISSION_PATH);

    /**
     * 检查发送者的权限。
     *
     * @param sender 发送者的ID，类型为Long。
     * @return 返回一个布尔值，表示权限检查的结果。如果权限检查通过，返回true；否则返回false。
     * @throws AuthException 如果在权限检查过程中发生异常，则抛出AuthException。
     */
    public static boolean checkPermission(Long sender) {
        try {
            // 尝试初始化并返回初始化结果
            return init(sender);
        } catch (Exception e) {
            // 如果初始化过程中发生异常，抛出授权异常
            throw new AuthException(MsgConstant.REQUEST_ERROR);
        }
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
    private static boolean init(Long sender) {
        try{
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                List<Long> senderList = new ArrayList<>();
                senderList.add(sender);

                LuckDayPermissionDTO dto = LuckDayPermissionDTO.builder()
                        .date(LocalDate.now())
                        .sender(senderList)
                        .build();
                String json = gson.toJson(dto);
                FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);

                return true;
            } else {
                JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.LUCKY_DAY_PERMISSION_PATH);
                LuckDayPermissionDTO dto = gson.fromJson(jsonObject, LuckDayPermissionDTO.class);

                if (dto.getDate().isEqual(LocalDate.now())) {
                    if (dto.getSender().contains(sender)) {
                        return false;
                    } else {
                        dto.getSender().add(sender);

                        String json = gson.toJson(dto);
                        FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);
                        return true;
                    }
                } else {
                    List<Long> senderList = new ArrayList<>();
                    senderList.add(sender);

                    dto.setDate(LocalDate.now());
                    dto.setSender(senderList);

                    String json = gson.toJson(dto);
                    FileManager.write(DataPathInfo.LUCKY_DAY_PERMISSION_PATH, json);

                    return true;
                }

            }
        } catch (IOException e) {
            throw new AuthException(MsgConstant.LUCKY_DAY_PERMISSION_ERROR);
        }

    }

}
