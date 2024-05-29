package org.kookies.mirai.pojo.entity.api.gaode.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressGetRequestBody {
    private String address;
    private String city;
}
