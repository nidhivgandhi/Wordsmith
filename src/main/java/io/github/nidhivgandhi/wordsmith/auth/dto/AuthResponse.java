package io.github.nidhivgandhi.wordsmith.auth.dto;

/**
 * What register and login both return. `tokenType` is spelled out so a client knows to
 * send `Authorization: Bearer <token>` without reading the docs.
 */
public record AuthResponse(String token, String tokenType, Long userId, String displayName) {

    public static AuthResponse bearer(String token, Long userId, String displayName) {
        return new AuthResponse(token, "Bearer", userId, displayName);
    }
}
