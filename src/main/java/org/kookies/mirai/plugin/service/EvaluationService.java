package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public interface EvaluationService {
    void evaluateSomebody(Member sender, Group group, String somebody);
}
