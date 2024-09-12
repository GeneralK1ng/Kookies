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
public class BeautifulGirlPermissionDTO implements Serializable {
    private LocalDate date;
    private Map<Long, Integer> senders;
}
