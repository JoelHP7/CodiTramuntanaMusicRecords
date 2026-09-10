package com.coditramuntana.musicrecords.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * A refresh token issued to a {@link UserAccount}.
 *
 * <p>Refresh tokens are persisted rather than kept stateless on purpose: storing them is what
 * makes revocation possible, so logging out genuinely invalidates the session instead of just
 * forgetting the token on the client side.
 *
 * <p>{@code expiresAt} is an epoch in milliseconds rather than a temporal type, for the same
 * reason the rest of the model avoids them: SQLite has no native date type, and round-tripping
 * temporal values through the community dialect has known rough edges.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Opaque random value handed to the client. Never included in {@code toString}.
     */
    @Column(nullable = false, unique = true, length = 200)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private UserAccount user;

    @Column(name = "expires_at", nullable = false)
    @ToString.Include
    private Long expiresAt;

    @Column(nullable = false)
    @Builder.Default
    @ToString.Include
    private boolean revoked = false;

    /**
     * A token is usable while it has not been revoked and has not expired yet.
     */
    public boolean isUsable(long nowEpochMs) {
        return !revoked && expiresAt != null && expiresAt > nowEpochMs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        RefreshToken refreshToken = (RefreshToken) other;
        return id != null && Objects.equals(id, refreshToken.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}
