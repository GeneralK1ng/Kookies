package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

/**
 * @author General_K1ng
 */
public interface EntertainmentService {
    /**
     * 答案之书
     * @param sender 发送者
     * @param group 所在群聊
     */
    void answer(Long sender, Group group);
}
