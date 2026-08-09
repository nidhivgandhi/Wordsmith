package io.github.nidhivgandhi.wordsmith.security;

/**
 * The principal we put in the SecurityContext once a token checks out.
 *
 * It holds the id and email straight from the token's claims rather than a loaded User
 * entity, so an authenticated request costs zero extra database queries. Anything that
 * genuinely needs the full row can look it up; most code only needs the id.
 */
public record AuthenticatedUser(Long id, String email) {}
