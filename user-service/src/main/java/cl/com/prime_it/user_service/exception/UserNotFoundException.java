package cl.com.prime_it.user_service.exception;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(final UUID id) {
        super("Could not find user with id: " + id);
    }
}
