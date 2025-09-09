package cl.com.prime_it.user_service.exception;


import cl.com.prime_it.user_service.exception.dto.ErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a,
                        HashMap::new));
        return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, fieldErrors);
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorMessage> handleBindException(BindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a,
                        HashMap::new));
        return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, fieldErrors);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> violations = new HashMap<>();
        Set<ConstraintViolation<?>> cvs = ex.getConstraintViolations();
        for (ConstraintViolation<?> v : cvs) {
            String key = v.getPropertyPath().toString();
            violations.put(key, v.getMessage());
        }
        return buildErrorResponse("Constraint validation failed", HttpStatus.BAD_REQUEST, violations);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String msg = String.format("Invalid value for parameter '%s'. Expected type: %s", ex.getName(), expected);
        return buildErrorResponse(msg, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleNotReadable(HttpMessageNotReadableException ex) {
        return buildErrorResponse("Cuerpo de la solicitud inválido o no legible", HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorMessage> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ErrorMessage> handleInvalidEmail(InvalidEmailFormatException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFound(UserNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ErrorMessage> handlePasswordValidationException(PasswordValidationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneric(Exception ex) {
        return buildErrorResponse("An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<ErrorMessage> buildErrorResponse(String message, HttpStatus status) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();
        return new ResponseEntity<>(errorMessage, status);
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(String message, HttpStatus status,
                                                            Map<String, String> details) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();
        return new ResponseEntity<>(errorMessage, status);
    }

}