package io.github.nidhivgandhi.wordsmith.auth;

import io.github.nidhivgandhi.wordsmith.auth.dto.LoginRequest;
import io.github.nidhivgandhi.wordsmith.auth.dto.RegisterRequest;
import io.github.nidhivgandhi.wordsmith.security.JwtProperties;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
import io.github.nidhivgandhi.wordsmith.user.User;
import io.github.nidhivgandhi.wordsmith.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Password handling, tested with a real BCryptPasswordEncoder rather than a mock —
 * mocking the encoder would make "we hashed it" trivially true and prove nothing.
 */
class AuthServiceTest {

    private UserRepository userRepo;
    private AuthService service;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        JwtService jwt = new JwtService(
                new JwtProperties("test-secret-that-is-at-least-32-bytes-long", 60));
        service = new AuthService(userRepo, encoder, jwt);
    }

    @Test
    void registerStoresAHashAndNeverThePlaintextPassword() {
        when(userRepo.existsByEmail("writer@example.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.register(new RegisterRequest("writer@example.com", "correct horse battery", "Nidhi"));

        var saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(saved.capture());

        String stored = saved.getValue().getPasswordHash();
        assertThat(stored).isNotEqualTo("correct horse battery");
        assertThat(stored).startsWith("$2a$");                       // a BCrypt hash
        assertThat(encoder.matches("correct horse battery", stored)).isTrue();
    }

    @Test
    void registerLowercasesEmailSoCaseVariantsCannotBecomeTwoAccounts() {
        when(userRepo.existsByEmail("writer@example.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.register(new RegisterRequest("  Writer@Example.COM  ", "correct horse battery", "Nidhi"));

        var saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("writer@example.com");
    }

    @Test
    void registerRejectsAnEmailThatAlreadyHasAnAccount() {
        when(userRepo.existsByEmail("writer@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("writer@example.com", "correct horse battery", "Nidhi")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepo, never()).save(any());
    }

    @Test
    void loginSucceedsWithTheRightPasswordAndFailsWithTheWrongOne() {
        User stored = new User();
        stored.setEmail("writer@example.com");
        stored.setDisplayName("Nidhi");
        stored.setPasswordHash(encoder.encode("correct horse battery"));
        when(userRepo.findByEmail("writer@example.com")).thenReturn(Optional.of(stored));

        assertThat(service.login(new LoginRequest("writer@example.com", "correct horse battery")).token())
                .isNotBlank();

        assertThatThrownBy(() -> service.login(new LoginRequest("writer@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void unknownEmailAndWrongPasswordFailIdentically() {
        User stored = new User();
        stored.setPasswordHash(encoder.encode("correct horse battery"));
        when(userRepo.findByEmail("known@example.com")).thenReturn(Optional.of(stored));
        when(userRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Same exception type, same message — nothing distinguishes "no such account"
        // from "wrong password", so login cannot be used to enumerate our users.
        var wrongPassword = org.assertj.core.api.Assertions.catchThrowable(
                () -> service.login(new LoginRequest("known@example.com", "wrong")));
        var noSuchUser = org.assertj.core.api.Assertions.catchThrowable(
                () -> service.login(new LoginRequest("unknown@example.com", "wrong")));

        assertThat(wrongPassword).isInstanceOf(InvalidCredentialsException.class);
        assertThat(noSuchUser).isInstanceOf(InvalidCredentialsException.class);
        assertThat(noSuchUser).hasMessage(wrongPassword.getMessage());
    }
}
