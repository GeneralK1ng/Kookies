package org.kookies.mirai.pojo.entity.api.gaode.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class POIResponse implements Serializable {
    private Integer count;

    private Integer status;

    private List<POI> pois;

    private String info;
}
