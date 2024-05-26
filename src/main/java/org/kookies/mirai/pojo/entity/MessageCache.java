package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MessageCache implements Serializable {
    private Long sender;

    private List<String> messages;
}
