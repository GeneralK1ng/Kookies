package org.kookies.mirai.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class VoiceRole implements Serializable {
    private String role;

    private String promptText;
}
