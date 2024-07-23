package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

/**
 * @author General_K1ng
 */
public interface SignInService {
    /**
     * 获取今日运势
     *
     * @param sender 发送者
     * @param group  群
     */
    void luckyDay(Long sender, Group group);

    /**
     * 获取今日老婆
     *
     * @param sender     发送者
     * @param group  群
     */
    void todayGirlFriend(long sender, Group group);

    /**
     * 获取摸鱼日报
     *
     * @param id     发送者
     * @param group  群
     */
    void messAroundDaily(long id, Group group);
}
