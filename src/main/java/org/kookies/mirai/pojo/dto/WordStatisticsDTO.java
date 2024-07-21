package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class WordStatisticsDTO implements Serializable {
    private List<String> top10Words;
    private List<Integer> top10Cnt;
}
