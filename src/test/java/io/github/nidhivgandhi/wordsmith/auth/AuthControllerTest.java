package io.github.nidhivgandhi.wordsmith.auth;

import io.github.nidhivgandhi.wordsmith.auth.dto.AuthResponse;
import io.github.nidhivgandhi.wordsmith.config.SecurityConfig;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.nidhivgandhi.wordsmith.support.TestAuth.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService service;

    @MockitoBean
    JwtService jwtService;

    @Test
    void registerReturns201WithAToken() throws Exception {
        when(service.register(any())).thenReturn(AuthResponse.bearer("a.b.c", 7L, "Nidhi"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","password":"correct horse battery",
                                 "displayName":"Nidhi"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("a.b.c"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    void registerRejectsBadEmailAndShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\",\"displayName\":\"N\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("email must be a valid email address"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("password must be between 8 and 72 characters"));

        verify(service, never()).register(any());
    }

    @Test
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        when(service.register(any()))
                .thenThrow(new EmailAlreadyUsedException("An account already exists for writer@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","password":"correct horse battery",
                                 "displayName":"Nidhi"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void loginReturns401WithAMessageThatDoesNotRevealWhetherTheEmailExists() throws Exception {
        when(service.login(any())).thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"writer@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                // Deliberately says nothing about which half was wrong.
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void registerAndLoginAreReachableWithoutAToken() throws Exception {
        when(service.login(any())).thenReturn(AuthResponse.bearer("a.b.c", 7L, "Nidhi"));

        // The obvious bootstrapping problem: you cannot send a token before you have one.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"writer@example.com\",\"password\":\"correct horse battery\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void meReturnsTheCallerAndRequiresAToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(user(7L, "writer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("writer@example.com"));

        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
