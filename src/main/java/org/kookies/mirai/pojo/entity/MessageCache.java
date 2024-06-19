package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;


import java.io.Serializable;
import java.util.List;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class MessageCache implements Serializable {
    private Long sender;

    private List<String> messages;
}
