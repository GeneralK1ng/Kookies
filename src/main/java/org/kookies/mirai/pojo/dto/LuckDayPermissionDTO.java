package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LuckDayPermissionDTO implements Serializable {
    private LocalDate date;

    private List<Long> sender;
}
