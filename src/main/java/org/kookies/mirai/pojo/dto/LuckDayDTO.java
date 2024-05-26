package org.kookies.mirai.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * LuckDayDTO类用于封装幸运日的相关信息，支持序列化。
 * 通过@Builder注解提供了一个构建器模式来创建对象实例，
 * 通过@Data注解自动生成了getter、setter、toString、hashCode、equals方法。
 */
@Data
@Builder
public class LuckDayDTO implements Serializable {
    private Long sender; // 发送者ID，用于标识发送幸运日信息的用户

    private Integer wealthFortune; // 财运指数，表示财运的幸运程度

    private Integer romanceFortune; // 桃花运指数，表示桃花运的幸运程度

    private Integer schoolFortune; // 学业运指数，表示学业运的幸运程度
}

