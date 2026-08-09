package io.github.nidhivgandhi.wordsmith.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Turns a valid `Authorization: Bearer <token>` header into an authenticated
 * SecurityContext for the duration of one request.
 *
 * OncePerRequestFilter, not plain Filter: a servlet container may dispatch the same
 * request more than once (forwards, async), and re-running auth on each pass is at best
 * wasted work and at worst subtly inconsistent.
 *
 * Note what this filter does NOT do: it never rejects anything. A missing or bad token
 * simply leaves the context unauthenticated, and the authorization rules in
 * SecurityConfig decide whether that is acceptable for the requested path. Keeping
 * "who are you" separate from "are you allowed" is what lets endpoints like group
 * search stay public without the filter needing to know about them.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            jwtService.parse(token).ifPresent(claims -> authenticate(request, claims));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, Claims claims) {
        AuthenticatedUser principal =
                new AuthenticatedUser(Long.valueOf(claims.getSubject()), claims.get("email", String.class));

        // Empty authority list: every authenticated user has the same powers here. Access
        // is decided by ownership of the specific row, not by a role, so roles would be
        // ceremony with nothing behind it.
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
