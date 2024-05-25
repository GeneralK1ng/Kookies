package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface AnswerBookService {
    /**
     * 答案之书
     * @param sender 发送者
     * @param group 所在群聊
     */
    void answer(Long sender, Group group);
}
