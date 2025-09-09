package cl.com.prime_it.user_service.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhoneResponse {
    private UUID uuid;
    private String number;
    private String cityCode;
    private String countryCode;


}