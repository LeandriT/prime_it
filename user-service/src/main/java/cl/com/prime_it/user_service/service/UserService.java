package cl.com.prime_it.user_service.service;

import cl.com.prime_it.user_service.dto.request.PartialUserRequest;
import cl.com.prime_it.user_service.dto.request.UserRequest;
import cl.com.prime_it.user_service.dto.response.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> index(Pageable pageable);

    UserResponse create(UserRequest userRequest);

    void update(UUID uuid, UserRequest userRequest);

    UserResponse partialUpdate(UUID uuid, PartialUserRequest userRequest);

    UserResponse show(UUID uuid);

    void delete(UUID uuid);


}
