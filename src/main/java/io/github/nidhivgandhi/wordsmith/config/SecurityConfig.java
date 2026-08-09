package io.github.nidhivgandhi.wordsmith.config;

import io.github.nidhivgandhi.wordsmith.security.JwtAuthenticationFilter;
import io.github.nidhivgandhi.wordsmith.web.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Spring Boot 4 ships Jackson 3, whose ObjectMapper lives in `tools.jackson.databind`.
// The Jackson 2 class of the same name (com.fasterxml.jackson.databind) is a different
// type entirely and has no bean here — an easy import to get wrong.
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    /**
     * BCrypt: deliberately slow, and slow in a way that scales. The work factor (10 by
     * default, ~100ms per hash) is stored inside each hash, so it can be raised later
     * without invalidating existing passwords. A fast hash like SHA-256 is the wrong
     * tool here precisely because it is fast — speed helps the attacker, not us.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                    ObjectMapper objectMapper) throws Exception {
        http
            // CSRF protects against a browser silently attaching *ambient* credentials
            // (cookies) to a forged cross-site request. A JWT in an Authorization header
            // is not ambient — an attacker's page cannot make the browser add it — so
            // for a token API CSRF tokens defend against nothing.
            .csrf(AbstractHttpConfigurer::disable)

            // No server-side session at all: every request re-proves who it is with its
            // token. That is what lets the API scale horizontally with no sticky sessions
            // and no shared session store.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // --- public ---
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Story structures are reference data, identical for everyone.
                .requestMatchers(HttpMethod.GET, "/api/structures", "/api/structures/**").permitAll()
                // Community discovery is public on purpose: you should be able to find a
                // writing group before committing to an account.
                .requestMatchers(HttpMethod.GET, "/api/groups", "/api/groups/**").permitAll()

                // --- everything else needs a valid token ---
                // Listed last, and as a catch-all, so a new endpoint added tomorrow is
                // private by default. Defaulting open is how endpoints leak.
                .anyRequest().authenticated())

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint(objectMapper))
                .accessDeniedHandler(accessDeniedHandler(objectMapper)))

            // Our filter runs before the username/password filter so that by the time
            // authorization is evaluated, the SecurityContext is already populated.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** No credentials, or bad ones, on a protected endpoint. -> 401 */
    private AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper mapper) {
        return (request, response, authException) ->
                writeError(mapper, response, HttpStatus.UNAUTHORIZED,
                        "Authentication required. Send an Authorization: Bearer <token> header.");
    }

    /** Valid credentials, insufficient rights. -> 403 */
    private AccessDeniedHandler accessDeniedHandler(ObjectMapper mapper) {
        return (request, response, deniedException) ->
                writeError(mapper, response, HttpStatus.FORBIDDEN, "Access denied");
    }

    /**
     * These two failures happen in the filter chain, before any controller runs, so
     * @RestControllerAdvice never sees them. Writing ApiError by hand here is what keeps
     * 401/403 looking like every other error the API returns.
     */
    private void writeError(ObjectMapper mapper, HttpServletResponse response,
                            HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), ApiError.of(status, message));
    }
}
