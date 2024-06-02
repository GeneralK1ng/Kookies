package org.kookies.mirai.pojo.entity.api.gaode.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class POI implements Serializable {
    private String name;

    private String location;

    private String address;

    //private String tag;

    private List<Photo> photos;
}
