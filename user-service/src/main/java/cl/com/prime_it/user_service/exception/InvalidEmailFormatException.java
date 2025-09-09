package cl.com.prime_it.user_service.exception;

public class InvalidEmailFormatException extends RuntimeException {

    public InvalidEmailFormatException(String email) {
        super("Invalid email format: " + email);
    }
}