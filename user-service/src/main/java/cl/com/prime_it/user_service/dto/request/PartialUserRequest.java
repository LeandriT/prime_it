package cl.com.prime_it.user_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PartialUserRequest {
    @NotNull(message = "active is required")
    Boolean active;

}
