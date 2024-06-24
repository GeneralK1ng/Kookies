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
import org.kookies.mirai.pojo.entity.Config;
import org.kookies.mirai.pojo.entity.Group;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author General_K1ng
 */
public class Permission {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    // TODO 后续需要对每个功能进行权限细分

    /**
     * 检查用户是否有权限
     *
     * @param sender 发送者ID，表示需要检查权限的用户
     * @param group 用户所属的群组ID，用于验证用户是否属于某个群组
     * @return 返回一个布尔值，如果检查通过返回true，方法中抛出异常则检查不通过
     * @throws AuthException 如果用户在黑名单中或者群组未启用，抛出此异常
     */
    public static boolean checkPermission(Long sender, Long group) {
        JsonObject jsonObject;
        try {
            jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
        Config config = GSON.fromJson(jsonObject, Config.class);

        // 检查用户是否在黑名单中
        if (config.getUserBlackList().contains(sender)) {
            throw new AuthException(MsgConstant.USER_IN_BLACK_LIST);
        }

        // 检查群组是否被启用
        if (!checkGroup(config, group)) {
            throw new AuthException(MsgConstant.GROUP_NOT_ENABLE);
        }

        return true;
    }

    /**
     * 检查指定的组是否在配置中被启用。
     * @param config 配置对象，包含启用的组信息。
     * @param group 要检查的组的ID。
     * @return 如果指定的组被启用，则返回true；否则返回false。
     */
    private static boolean checkGroup(Config config, Long group) {
        // 遍历配置中启用的组列表
        for (Group g : config.getEnableGroupList()) {
            // 如果找到与指定ID匹配的组，则返回true
            if (g.getId().equals(group)) {
                return true;
            }
        }
        // 如果没有找到与指定ID匹配的组，则返回false
        return false;
    }

}
