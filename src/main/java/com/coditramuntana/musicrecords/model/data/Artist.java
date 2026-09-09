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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A musical artist owning a collection of LPs.
 *
 * <p>{@code equals}, {@code hashCode} and {@code toString} are restricted to explicitly
 * included fields: including the {@code lps} collection would cause infinite recursion
 * through the bidirectional graph and would force the initialisation of lazy associations.
 */
@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

}
