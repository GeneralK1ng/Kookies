package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PoiDTO implements Serializable {
    private Long id;

    private String newType;

    private String bigCategory;

    private String midCategory;

    private String subCategory;
}
