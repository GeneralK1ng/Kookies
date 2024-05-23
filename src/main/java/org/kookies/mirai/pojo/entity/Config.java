package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class Config implements Serializable {
    private BotInfo botInfo;
    private List<Long> adminList;
}
