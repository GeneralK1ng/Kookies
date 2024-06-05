package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class EatWhatDTO implements Serializable {
    // TODO 后续要做一个缓存队列，这是缓存的DTO
}
