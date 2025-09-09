package cl.com.prime_it.user_service.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(final String email) {
        super("Email is already registered: " + email);
    }
}
