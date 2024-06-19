package org.kookies.mirai.pojo.entity;

import lombok.Data;
import org.kookies.mirai.pojo.entity.api.BaiduApiConfig;
import org.kookies.mirai.pojo.entity.api.GaodeApiConfig;
import org.kookies.mirai.pojo.entity.api.VoiceApiConfig;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author General_K1ng
 */
@Data
public class BotInfo implements Serializable {

    // 机器人生日
    private LocalDate birthday;

    // 机器人名字
    private String name;

    // 百度api配置
    private BaiduApiConfig baiduApiConfig;

    // 高德api配置
    private GaodeApiConfig gaodeApiConfig;

    // 语音api配置
    private VoiceApiConfig voiceApiConfig;

    // 菜鸟Api
    private String runoobToken;
}
