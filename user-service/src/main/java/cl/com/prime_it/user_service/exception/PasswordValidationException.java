package cl.com.prime_it.user_service.exception;

public class PasswordValidationException extends RuntimeException {

    public PasswordValidationException(String password) {
        super("Password is not strong: " + password);
    }
}