package org.kookies.mirai.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Group implements Serializable {
    private Long id;
    private List<String> tag;
}
