package org.kookies.mirai.pojo.entity.api.response.runoob;

import lombok.Builder;
import lombok.Data;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class CodeRunResponse {

    private String output;

    private String errors;
}
