package io.github.nidhivgandhi.wordsmith.auth;

import io.github.nidhivgandhi.wordsmith.auth.dto.AuthResponse;
import io.github.nidhivgandhi.wordsmith.auth.dto.LoginRequest;
import io.github.nidhivgandhi.wordsmith.auth.dto.RegisterRequest;
import io.github.nidhivgandhi.wordsmith.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    /**
     * Lets a client check whether the token it is holding is still good, without having
     * to make a real request and interpret a 401. Useful for a frontend deciding whether
     * to show a logged-in view on page load.
     */
    @GetMapping("/me")
    public AuthenticatedUser me(@AuthenticationPrincipal AuthenticatedUser user) {
        return user;
    }
}
