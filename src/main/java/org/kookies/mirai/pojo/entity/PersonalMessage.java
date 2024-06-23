package org.kookies.mirai.pojo.entity;

import lombok.Builder;
import lombok.Data;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class PersonalMessage implements Serializable {
    private LocalDate date;

    private List<String> messages;

    public static final Integer EVALUATION_HISTORY_SIZE = 30;
}
