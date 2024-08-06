package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

/**
 * @author General_K1ng
 */
public interface ConvenienceService {

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

    /**
     * 根据提供的代码和编程语言，运行代码并返回运行结果。
     *
     * @param sender 发送请求的用户ID
     * @param group 目标群组
     * @param code 提供的代码
     * @param lang 提供的编程语言
     */
    void codeRun(long sender, Group group, String code, String lang);

    /**
     * 获取当前奥运信息
     *
     * @param sender 发送请求的用户ID
     * @param group 目标群组
     */
    void olympicDaily(long sender, Group group);
}
