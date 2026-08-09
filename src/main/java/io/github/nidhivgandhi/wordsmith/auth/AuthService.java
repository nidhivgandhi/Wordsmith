package io.github.nidhivgandhi.wordsmith.auth;

import io.github.nidhivgandhi.wordsmith.auth.dto.AuthResponse;
import io.github.nidhivgandhi.wordsmith.auth.dto.LoginRequest;
import io.github.nidhivgandhi.wordsmith.auth.dto.RegisterRequest;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
import io.github.nidhivgandhi.wordsmith.user.User;
import io.github.nidhivgandhi.wordsmith.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.email());

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("An account already exists for " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setDisplayName(req.displayName());
        // encode(), never the raw password. BCrypt generates its own random salt per call
        // and stores it inside the hash string, so two identical passwords hash differently
        // and one leaked table cannot be attacked with a precomputed rainbow table.
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        User saved = userRepo.save(user);
        return AuthResponse.bearer(
                jwtService.generateToken(saved.getId(), saved.getEmail()),
                saved.getId(),
                saved.getDisplayName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(normalizeEmail(req.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // matches() re-hashes the supplied password with the salt embedded in the stored
        // hash and compares in constant time, so it leaks nothing through timing.
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return AuthResponse.bearer(
                jwtService.generateToken(user.getId(), user.getEmail()),
                user.getId(),
                user.getDisplayName());
    }

    /**
     * Emails are case-insensitive in practice, so store and compare them lowercased —
     * otherwise Nidhi@example.com and nidhi@example.com become two accounts, and the
     * UNIQUE constraint on the column happily allows it.
     */
    private static String normalizeEmail(String email) {
        // Locale.ROOT, not the default locale: in a Turkish locale "I".toLowerCase()
        // yields a dotless "ı", so the same email would normalize differently depending
        // on where the server happens to be running.
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
