package io.github.nidhivgandhi.wordsmith.auth;

/** Registration attempted with an email that already has an account. -> 409 */
public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
