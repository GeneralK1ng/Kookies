package org.kookies.mirai.plugin.service;

import net.mamoe.mirai.contact.Group;

public interface EatWhatService {

    void eatWhat(long sender, Group group, String address, String city);
}
