package io.github.nidhivgandhi.wordsmith.auth;

/**
 * Login failed. One exception covers both "no such email" and "wrong password", and the
 * handler gives them one identical message — telling them apart lets an attacker
 * enumerate which email addresses have accounts.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
