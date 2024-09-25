package org.kookies.mirai.pojo.entity.api.request.baidu.imageRec;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendImageRec {
    private String url;
    private String question;
}
