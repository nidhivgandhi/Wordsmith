package io.github.nidhivgandhi.wordsmith.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Mints and verifies JSON Web Tokens.
 *
 * A JWT is three base64 segments: header, claims, signature. The first two are only
 * encoded, NOT encrypted — anyone holding a token can read the claims. What the
 * signature guarantees is that nobody *changed* them without our secret key. So the
 * rule is: claims are public, and the secret must stay secret.
 *
 * We sign with HS256 (one shared secret, used to both sign and verify), which suits a
 * single service signing its own tokens. Asymmetric RS256 only earns its complexity
 * when some other party needs to verify without being able to mint.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(JwtProperties props) {
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        // Fail loudly at startup rather than at the first login: HS256 requires a key of
        // at least 256 bits, and a short secret is a weak secret.
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "wordsmith.jwt.secret must be at least 32 bytes for HS256 (got "
                            + secretBytes.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofMinutes(props.expirationMinutes());
    }

    /**
     * The subject claim is the user id, not the email. Emails can change; the id cannot,
     * and a token that outlives an email change should still identify the right person.
     */
    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /**
     * Returns the token's claims if it is genuine and unexpired, otherwise empty.
     *
     * Every failure — bad signature, expired, malformed, forged "alg: none" header —
     * collapses to the same empty result on purpose. The caller cannot accidentally
     * treat "expired" as "fine", and we never tell an attacker which part they got wrong.
     */
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(key)      // signature checked here; parsing an unverified token is the classic JWT bug
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
