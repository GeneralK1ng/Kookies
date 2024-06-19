package org.kookies.mirai.pojo.entity.api;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class VoiceApiConfig implements Serializable {
    // 这是快速开发做出的妥协
    // TODO 后续需要使用枚举类型，设定不同角色和语气，后面还需要改
    private String apiUrl;

    private String ref_audio_path;

    private String prompt_text;

    private String gpt_weights_path;

    private String sovits_weights_path;
}
