package io.github.nidhivgandhi.wordsmith.web;

/**
 * Thrown when a requested resource (novel, beat, structure, ...) does not exist.
 * The GlobalExceptionHandler maps this to a 404 Not Found response, so service
 * and controller code can just throw it and stay out of the HTTP-status business.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
