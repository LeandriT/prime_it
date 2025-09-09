package cl.com.prime_it.user_service.dto.request;

import cl.com.prime_it.user_service.dto.retention.OnCreate;
import cl.com.prime_it.user_service.dto.retention.OnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class UserRequest {
    @NotBlank(message = "Name must not be blank", groups = {OnCreate.class, OnUpdate.class})
    @NotNull(message = "Name is required", groups = {OnCreate.class, OnUpdate.class})
    String name;

    @NotNull(message = "email is required", groups = {OnCreate.class})
    @Email(groups = {OnCreate.class})
    String email;

    @NotBlank(message = "Password must not be blank", groups = {OnCreate.class})
    @NotNull(message = "password is required", groups = {OnCreate.class})
    String password;

    @NotNull(message = "phones is required", groups = {OnCreate.class})
    @Size(min = 1, message = "at least one phone is required", groups = {OnCreate.class})
    @Singular
    List<PhoneRequest> phones;
}
