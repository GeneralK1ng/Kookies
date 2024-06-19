package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class TodayGirlPermissionDTO implements Serializable {
    private LocalDate date;

    private Map<Long, Integer> senderWithTimes;
}
