package org.kookies.mirai.pojo.entity;

import lombok.Data;
import org.kookies.mirai.pojo.entity.api.baidu.BaiduApiConfig;
import org.kookies.mirai.pojo.entity.api.gaode.GaodeApiConfig;
import org.kookies.mirai.pojo.entity.api.voice.VoiceApiConfig;

import java.io.Serializable;
import java.time.LocalDate;

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
}
