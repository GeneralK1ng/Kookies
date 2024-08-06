package org.kookies.mirai.pojo.entity.api.response.baidu.olympic;

import lombok.Data;

import java.io.Serializable;

/**
 * @author General_K1ng
 */
@Data
public class OlympicDataResponse implements Serializable {
    private String countryName;
    private Integer rank;
    private Integer gold;
    private Integer silver;
    private Integer bronze;
    private Integer total;
}
