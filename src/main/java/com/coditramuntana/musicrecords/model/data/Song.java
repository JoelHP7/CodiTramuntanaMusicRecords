package com.coditramuntana.musicrecords.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
 * A song contained in an {@link Lp} and written by one or more {@link Author}s.
 *
 * <p>This is the owning side of the many-to-many association with authors.
 */
@Entity
@Table(
        name = "songs",
        uniqueConstraints = @UniqueConstraint(name = "uk_song_lp_name", columnNames = {"lp_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 150)
    @ToString.Include
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lp_id", nullable = false, foreignKey = @ForeignKey(name = "fk_song_lp"))
    private Lp lp;

    /**
     * Authors of this song. No cascade on purpose: authors are a shared catalogue, so
     * removing a song must never remove the authors it links to.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_authors",
            joinColumns = @JoinColumn(name = "song_id", foreignKey = @ForeignKey(name = "fk_song_authors_song")),
            inverseJoinColumns = @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_song_authors_author")),
            uniqueConstraints = @UniqueConstraint(name = "uk_song_author", columnNames = {"song_id", "author_id"})
    )
    @Builder.Default
    private Set<Author> authors = new LinkedHashSet<>();

    /**
     * Replaces the whole set of authors of this song.
     */
    public void replaceAuthors(Set<Author> newAuthors) {
        authors.clear();
        authors.addAll(newAuthors);
    }

    /**
     * Equality by persisted identifier; transient instances are only equal to themselves.
     *
     * <p>This matters here more than anywhere else: several brand new songs are added to
     * the same {@code Set} before being flushed, and comparing them only by a still null
     * id would collapse them into a single element.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Song song = (Song) other;
        return id != null && Objects.equals(id, song.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}
