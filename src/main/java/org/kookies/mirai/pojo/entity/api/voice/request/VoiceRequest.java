package org.kookies.mirai.pojo.entity.api.voice.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoiceRequest {
    private String ref_audio_path;

    private String prompt_text;

    private String prompt_lang;

    private String text;

    private String text_lang;

    private String gpt_weights_path;

    private String sovits_weights_path;
}
