package org.kookies.mirai.pojo.entity.api.voice;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class VoiceApiConfig implements Serializable {
    private String apiUrl;

    private String ref_audio_path;

    private String prompt_text;

    private String gpt_weights_path;

    private String sovits_weights_path;
}
