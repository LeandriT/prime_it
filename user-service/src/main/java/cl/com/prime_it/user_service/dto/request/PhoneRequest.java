package cl.com.prime_it.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhoneRequest {
    @NotBlank(message = "number must not be blank")
    @NotNull(message = "number is required")
    String number;

    @NotBlank(message = "city code must not be blank")
    @NotNull(message = "city code is required")
    String cityCode;

    @NotBlank(message = "country code must not be blank")
    @NotNull(message = "country code is required")
    String countryCode;
}
