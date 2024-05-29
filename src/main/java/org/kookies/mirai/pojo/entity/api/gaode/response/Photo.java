package org.kookies.mirai.pojo.entity.api.gaode.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Photo implements Serializable {
    private List<String> title;

    private String url;
}
