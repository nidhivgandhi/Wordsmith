package io.github.nidhivgandhi.wordsmith.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the `wordsmith.jwt.*` block from application.yml. Typed config beats scattered
 * @Value strings: the values are validated once, at startup, in one place.
 */
@ConfigurationProperties(prefix = "wordsmith.jwt")
public record JwtProperties(String secret, long expirationMinutes) {}
