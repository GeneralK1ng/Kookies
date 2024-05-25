package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LuckDayDTO implements Serializable {
    private Long sender;

    private Integer wealthFortune;

    private Integer romanceFortune;

    private Integer schoolFortune;
}
