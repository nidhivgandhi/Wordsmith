package io.github.nidhivgandhi.wordsmith.user;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Maps the `users` table created back in V1. Only the columns auth needs are mapped
 * for now; `location`, `city` and `timezone` exist in the table and get entity fields
 * when the features that use them land (nearby-groups-for-me, streak reminders).
 */
@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    /**
     * A BCrypt hash, never a password. Named to make that obvious at every call site —
     * there is deliberately no field anywhere that holds a plaintext password.
     */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
