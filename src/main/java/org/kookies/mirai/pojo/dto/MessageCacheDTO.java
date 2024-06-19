package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;
import org.kookies.mirai.pojo.entity.MessageCache;

import java.time.LocalDate;
import java.util.List;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class MessageCacheDTO {
    private LocalDate date;

    private List<MessageCache> messageCaches;
}
