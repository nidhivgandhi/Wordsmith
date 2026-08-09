package io.github.nidhivgandhi.wordsmith.web;

import io.github.nidhivgandhi.wordsmith.auth.EmailAlreadyUsedException;
import io.github.nidhivgandhi.wordsmith.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One place that turns exceptions thrown anywhere in the controllers into clean
 * HTTP responses. @RestControllerAdvice = @ControllerAdvice + @ResponseBody, so
 * the returned ApiError is serialized to JSON automatically.
 *
 * Spring matches the most specific @ExceptionHandler to the thrown type, so order
 * of methods here does not matter.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** A resource we looked up by id was not there. -> 404 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * Bean Validation (@NotBlank, @NotNull, ...) on a @Valid @RequestBody failed.
     * We pull each rejected field + its message into a map so the client knows
     * exactly what to fix. -> 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiError.validation(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors));
    }

    /**
     * A client sent semantically bad input that our code rejected with
     * IllegalArgumentException (e.g. a structureId that does not exist, or a beat
     * that does not belong to the given novel). -> 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Registration with an email that already has an account. -> 409 Conflict, not 400:
     * the request itself is well-formed, it just conflicts with the current state.
     */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * Bad login. -> 401. The message is identical whether the email is unknown or the
     * password is wrong; distinguishing them turns the login form into a tool for
     * discovering which email addresses have accounts here.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    // Deliberately no catch-all @ExceptionHandler(Exception.class): a broad handler
    // would also swallow framework exceptions like NoResourceFoundException (unknown
    // URL) and turn Spring's built-in 404 into our 500. Unexpected exceptions fall
    // through to Spring's default handler, which already returns a 500.
}
