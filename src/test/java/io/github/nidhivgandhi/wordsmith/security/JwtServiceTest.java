package io.github.nidhivgandhi.wordsmith.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The token layer, tested without Spring: JwtService is a plain object, so these run in
 * milliseconds and cover the cases that matter most — the ones where a token should be
 * rejected.
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";

    private static JwtService serviceWith(String secret, long minutes) {
        return new JwtService(new JwtProperties(secret, minutes));
    }

    private final JwtService jwt = serviceWith(SECRET, 60);

    @Test
    void roundTripsUserIdAndEmail() {
        String token = jwt.generateToken(42L, "writer@example.com");

        Optional<Claims> claims = jwt.parse(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("42");
        assertThat(claims.get().get("email", String.class)).isEqualTo("writer@example.com");
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        // The attack this prevents: mint your own token claiming to be user 1.
        JwtService attacker = serviceWith("a-completely-different-secret-32-bytes!", 60);
        String forged = attacker.generateToken(1L, "victim@example.com");

        assertThat(jwt.parse(forged)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        // Negative lifetime: issued now, expired an hour ago.
        String stale = serviceWith(SECRET, -60).generateToken(42L, "writer@example.com");

        assertThat(jwt.parse(stale)).isEmpty();
    }

    @Test
    void rejectsTamperedAndMalformedTokens() {
        String token = jwt.generateToken(42L, "writer@example.com");

        // Flipping any character breaks the signature over the payload.
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(jwt.parse(tampered)).isEmpty();
        assertThat(jwt.parse("not-a-jwt")).isEmpty();
        assertThat(jwt.parse("")).isEmpty();
    }

    @Test
    void refusesToStartWhenTheSecretEnvVarWasNeverSet() {
        // Spring passes an unresolved placeholder through as a literal string rather
        // than failing, so this is what an unset JWT_SECRET actually looks like in prod.
        assertThatThrownBy(() -> serviceWith("${JWT_SECRET}", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set");

        assertThatThrownBy(() -> serviceWith("   ", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set");
    }

    @Test
    void refusesToStartWithATooShortSecret() {
        // Caught at construction, so a weak secret fails deployment rather than quietly
        // producing weak tokens.
        assertThatThrownBy(() -> serviceWith("too-short", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
