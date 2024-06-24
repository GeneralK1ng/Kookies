package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

/**
 * @author General_K1ng
 */
public interface EvaluationService {
    /**
     * 评价某人
     *
     * @param sender   发送者
     * @param group    群
     * @param somebody 被评价者
     */
    void evaluateSomebody(Member sender, Group group, String somebody);
}
