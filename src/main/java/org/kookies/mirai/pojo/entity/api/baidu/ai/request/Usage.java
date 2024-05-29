package org.kookies.mirai.pojo.entity.api.baidu.ai.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class Usage implements Serializable {
    // 提示词消耗的token
    private Integer prompt_tokens;

    // 回答消耗的token
    private Integer completion_tokens;

    // 总消耗的token
    private Integer total_tokens;
}
