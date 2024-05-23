package org.kookies.mirai.commen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.context.ConfigContext;
import org.kookies.mirai.commen.exceptions.ConfigurationLoadException;
import org.kookies.mirai.commen.info.ConfigurationInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.entity.Config;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;


public class ConfigurationLoader {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    private static final File file = new File(ConfigurationInfo.CONFIG_PATH);

    /**
     * 对外开放的方法
     * 用于加载配置文件
     */
    public static void init() {
        // 如果配置文件不存在，则按照 resources/template下的 configurationTemplate.json 创建一个配置文件
        try {
            if (!file.exists()){
                file.getParentFile().mkdirs();
                String templateFile = FileManager.readTemplateFile(ConfigurationInfo.CONFIG_TEMPLATE_PATH);
                FileManager.write(file.getPath(), templateFile);
            } else {
                update();
            }

            // 读取配置文件并解析为 Config 对象
            JsonObject jsonObject = FileManager.readJsonFile(file.getPath());
            Config config = gson.fromJson(jsonObject, Config.class);
            ConfigContext.setConfig(config);  // 设置到 ThreadLocal 中
        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
    }

    /**
     * 用于更新配置文件
     */
    private static void update() {
        try {
            JsonObject jsonObject = FileManager.readJsonFile(file.getPath());
            FileManager.writeJsonFile(file.getPath(), jsonObject);
        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.CONFIG_UPDATE_ERROR);
        }
    }
}
