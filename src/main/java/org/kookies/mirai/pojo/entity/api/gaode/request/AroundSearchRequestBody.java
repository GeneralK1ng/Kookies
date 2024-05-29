package org.kookies.mirai.pojo.entity.api.gaode.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AroundSearchRequestBody {
    // 经纬度 用 “,” 分开 经度在前，维度在后
    private String location;

    // POI
    private String types;

    // 半径
    private Integer radius;

    // 排序规则
    // distance:距离排序
    // weight:综合排序
    private String sortrule;

}
