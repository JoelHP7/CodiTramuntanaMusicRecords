package com.coditramuntana.musicrecords.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * A user allowed to modify the discography.
 *
 * <p>Named {@code UserAccount} rather than {@code User} to avoid clashing with
 * {@link org.springframework.security.core.userdetails.User} in every import block.
 *
 * <p>The password hash is deliberately left out of {@code toString}: the class uses
 * {@code onlyExplicitlyIncluded}, so a stray {@code log.info("{}", user)} can never leak it.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    @ToString.Include
    private String username;

    /**
     * BCrypt hash, never the plain password.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Equality by persisted identifier; transient instances are only equal to themselves.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        UserAccount account = (UserAccount) other;
        return id != null && Objects.equals(id, account.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}
