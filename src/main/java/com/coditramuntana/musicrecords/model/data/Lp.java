package com.coditramuntana.musicrecords.model.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * A long play record belonging to a single {@link Artist} and holding its {@link Song}s.
 *
 * <p>An LP is the aggregate root of its songs: songs have no identity outside the LP,
 * so the association cascades and removes orphans.
 */
@Entity
@Table(
        name = "lps",
        uniqueConstraints = @UniqueConstraint(name = "uk_lp_artist_name", columnNames = {"artist_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Lp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 150)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Owning artist. {@code @ManyToOne} is EAGER by default in JPA, which is the most
     * common source of N+1 queries, so LAZY is set explicitly.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lp_artist"))
    private Artist artist;

    /**
     * Songs of this LP. Modelled as a {@code Set} so that both this collection and
     * {@code Song.authors} can be fetch-joined in the same query without triggering
     * {@code MultipleBagFetchException}.
     */
    @OneToMany(mappedBy = "lp", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Song> songs = new LinkedHashSet<>();

    /**
     * Adds a song keeping both sides of the association in sync.
     */
    public void addSong(Song song) {
        songs.add(song);
        song.setLp(this);
    }

    /**
     * Removes a song keeping both sides in sync. Orphan removal deletes it from the database.
     */
    public void removeSong(Song song) {
        songs.remove(song);
        song.setLp(null);
    }

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
        Lp lp = (Lp) other;
        return id != null && Objects.equals(id, lp.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}
