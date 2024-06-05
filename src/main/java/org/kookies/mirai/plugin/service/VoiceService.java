package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;
import org.kookies.mirai.pojo.entity.VoiceRole;

public interface VoiceService {
    /**
     * 快说
     * @param id 发送者id
     * @param group 群
     * @param content 说的内容
     */
    void say(long id, Group group, String content);

    /**
     * 获取语音角色
     * @param name 角色名称
     * @return 语音角色
     */
    VoiceRole getVoiceRole(String name);

    /**
     * 根据角色获取语音
     * <p>
     * @param id 发送者id
     * @param group 群
     * @param voiceRole 语音角色
     * @param s 说的内容
     */
    void say(long id, Group group, VoiceRole voiceRole, String s);
}
