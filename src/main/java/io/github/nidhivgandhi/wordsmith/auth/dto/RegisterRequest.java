package io.github.nidhivgandhi.wordsmith.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        // Length is the honest lever on password strength; composition rules ("one
        // symbol, one digit") mostly push people toward Passw0rd! and away from
        // passphrases. BCrypt silently truncates past 72 bytes, so cap it there.
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "displayName is required")
        @Size(max = 100, message = "displayName must be at most 100 characters")
        String displayName) {}
