package org.kookies.mirai.pojo.entity.ai.baidu;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {

    // 当前支持以下：
    // user: 表示用户
    // assistant: 表示对话助手
    @Expose
    private String role;

    // 对话内容，不能为空
    @Expose
    private String content;
}
