package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface AnswerBookService {
    void answer(Long sender, Group group);
}
