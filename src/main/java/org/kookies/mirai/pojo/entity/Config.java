package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Config {
    private LocalDate birthday;

    private String name;

}
