package io.github.nidhivgandhi.wordsmith.support;

import io.github.nidhivgandhi.wordsmith.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Puts an AuthenticatedUser into the SecurityContext for a MockMvc request.
 *
 * Spring's @WithMockUser is not usable here: it installs a String (or UserDetails)
 * principal, while our controllers resolve @AuthenticationPrincipal AuthenticatedUser.
 * Building the Authentication ourselves keeps the test principal identical to the one
 * JwtAuthenticationFilter produces in production.
 */
public final class TestAuth {

    private TestAuth() {}

    public static RequestPostProcessor user(Long id) {
        return user(id, "writer" + id + "@example.com");
    }

    public static RequestPostProcessor user(Long id, String email) {
        var principal = new AuthenticatedUser(id, email);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
