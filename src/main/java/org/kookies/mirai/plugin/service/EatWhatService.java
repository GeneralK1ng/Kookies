package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface EatWhatService {

    /**
     * 根据提供的地址和城市信息，查询附近的一个点位（Point of Interest, POI），
     * 并向指定群组中的发送者发送关于该点位的消息。
     *
     * @param sender 发送请求的用户ID
     * @param group 目标群组
     * @param address 提供的地址信息
     * @param city 提供的城市信息
     */
    void eatWhat(long sender, Group group, String address, String city);
}
