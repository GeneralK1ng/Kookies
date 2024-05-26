package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface LuckyDayService {
    /**
     * 获取今日运势
     *
     * @param sender 发送者
     * @param group  群
     */
    void luckyDay(Long sender, Group group);
}
