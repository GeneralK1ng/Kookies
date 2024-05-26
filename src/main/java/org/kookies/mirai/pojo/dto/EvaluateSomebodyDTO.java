package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class EvaluateSomebodyDTO implements Serializable {
    private String nameCard;

    private String nick;

    private List<String> historyMsg;
}
