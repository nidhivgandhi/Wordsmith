package io.github.nidhivgandhi.wordsmith.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Deliberately looser than RegisterRequest: login validates the credentials against
 * what is stored, not against today's rules. Enforcing the current password policy
 * here would lock out anyone who registered under an older one.
 */
public record LoginRequest(
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password) {}
