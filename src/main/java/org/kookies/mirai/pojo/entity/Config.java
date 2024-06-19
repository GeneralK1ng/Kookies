package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class Config implements Serializable {
    // 机器人信息
    private BotInfo botInfo;

    // 每日老婆请求次数
    private Integer maxTodayGirlTimes;

    // 管理员列表
    private List<Long> adminList;

    // 用户黑名单
    private List<Long> userBlackList;

    // 启用群组
    private List<Group> enableGroupList;
}
