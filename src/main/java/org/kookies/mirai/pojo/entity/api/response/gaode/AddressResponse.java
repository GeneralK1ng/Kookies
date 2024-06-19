package org.kookies.mirai.pojo.entity.api.response.gaode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private String formatted_address;
    private String location;
}
