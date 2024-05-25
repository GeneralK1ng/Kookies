package org.kookies.mirai.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Group implements Serializable {
    // 群号
    private Long id;

    // 群标签
    private List<String> tag;
}
