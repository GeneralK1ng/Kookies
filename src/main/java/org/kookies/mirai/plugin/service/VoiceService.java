package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface VoiceService {
    /**
     * 快说
     * @param id 发送者id
     * @param group 群
     * @param content 说的内容
     */
    void say(long id, Group group, String content);
}
