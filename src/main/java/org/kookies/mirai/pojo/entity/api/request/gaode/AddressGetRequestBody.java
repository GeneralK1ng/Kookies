package org.kookies.mirai.pojo.entity.api.request.gaode;

import lombok.Builder;
import lombok.Data;

/**
 * @author General_K1ng
 */
@Data
@Builder
public class AddressGetRequestBody {
    private String address;
    private String city;
}
