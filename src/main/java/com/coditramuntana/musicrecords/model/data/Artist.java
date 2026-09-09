package com.coditramuntana.musicrecords.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A musical artist owning a collection of LPs.
 *
 * <p>{@code toString} is restricted to explicitly included fields, and
 * {@code equals}/{@code hashCode} are written by hand: including the {@code lps}
 * collection would cause infinite recursion through the bidirectional graph and would
 * force the initialisation of lazy associations. See {@link #equals(Object)} for why the
 * generated "compare every field" implementation is not usable in a JPA entity.
 */
@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * LPs of this artist. Deliberately without cascade or orphan removal: deleting an
     * artist that still owns LPs is rejected with a conflict instead of silently
     * destroying the whole discography.
     */
    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Lp> lps = new LinkedHashSet<>();

    /**
     * Adds an LP keeping both sides of the association in sync.
     */
    public void addLp(Lp lp) {
        lps.add(lp);
        lp.setArtist(this);
    }

    /**
     * Removes an LP keeping both sides of the association in sync.
     */
    public void removeLp(Lp lp) {
        lps.remove(lp);
        lp.setArtist(null);
    }

    /**
     * Entities are equal when they share a persisted identifier. Transient instances
     * (with a {@code null} id) are only equal to themselves, so several new entities can
     * coexist inside the same {@code Set} before being flushed.
     *
     * <p>{@code Hibernate.getClass} is used instead of {@code getClass} so an entity and
     * its lazy proxy compare as equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Artist artist = (Artist) other;
        return id != null && Objects.equals(id, artist.getId());
    }

    /**
     * Constant per type: the identifier is assigned on persist, so a value derived from
     * it would change while the entity sits inside a hash based collection.
     */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}
