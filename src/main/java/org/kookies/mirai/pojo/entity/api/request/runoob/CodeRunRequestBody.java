package org.kookies.mirai.pojo.entity.api.request.runoob;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class CodeRunRequestBody implements Serializable {
    private String code;

    private String fileext;

    private String token;
}
