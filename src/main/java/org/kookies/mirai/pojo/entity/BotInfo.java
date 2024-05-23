package org.kookies.mirai.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class BotInfo implements Serializable {
    private LocalDate birthday;

    private String name;
}
