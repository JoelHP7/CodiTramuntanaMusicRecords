package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.projection.LpAuthorRow;
import com.coditramuntana.musicrecords.model.projection.LpReportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LpRepository extends JpaRepository<Lp, Long> {

    /**
     * Number of LPs of an artist. Used instead of {@code artist.getLps().size()} so the
     * artist detail page issues a COUNT statement instead of loading the whole collection.
     */
    long countByArtistId(Long artistId);

    boolean existsByArtistIdAndNameIgnoreCase(Long artistId, String name);

    boolean existsByArtistIdAndNameIgnoreCaseAndIdNot(Long artistId, String name, Long id);

    /**
     * The {@code join fetch} is required: without it, reading the artist name while
     * mapping each LP would trigger one extra query per row.
     */
    @Query("select lp from Lp lp join fetch lp.artist a order by a.name asc, lp.name asc")
    List<Lp> findAllWithArtist();

    @Query("""
            select lp from Lp lp
            join fetch lp.artist a
            where lower(a.name) like lower(concat('%', :artistName, '%'))
            order by a.name asc, lp.name asc
            """)
    List<Lp> findByArtistNameContaining(@Param("artistName") String artistName);

    @Query("select lp from Lp lp join fetch lp.artist a where a.id = :artistId order by lp.name asc")
    List<Lp> findByArtistId(@Param("artistId") Long artistId);

    /**
     * Full detail of one LP in a single statement. Fetching two collections at once is
     * legal here because both are {@code Set}s; with {@code List}s Hibernate would fail
     * with MultipleBagFetchException.
     */
    @Query("""
            select lp from Lp lp
            join fetch lp.artist
            left join fetch lp.songs s
            left join fetch s.authors
            where lp.id = :id
            """)
    Optional<Lp> findDetailById(@Param("id") Long id);

    /**
     * First half of the discography report: one row per LP with its song count.
     *
     * <p>{@code left join} keeps LPs without songs in the report, and {@code count(s.id)}
     * (instead of {@code count(*)}) makes those rows report 0 rather than 1.
     */
    @Query("""
            select new com.coditramuntana.musicrecords.model.projection.LpReportRow(
                       lp.id, lp.name, a.name, count(s.id))
            from Lp lp
            join lp.artist a
            left join lp.songs s
            group by lp.id, lp.name, a.name
            order by a.name asc, lp.name asc
            """)
    List<LpReportRow> findReportRows();

    /**
     * Second half of the discography report: the distinct authors of each LP, already
     * ordered so the rendered list is deterministic and therefore assertable in tests.
     */
    @Query("""
            select distinct new com.coditramuntana.musicrecords.model.projection.LpAuthorRow(
                       lp.id, au.name)
            from Lp lp
            join lp.songs s
            join s.authors au
            order by lp.id asc, au.name asc
            """)
    List<LpAuthorRow> findReportAuthorRows();

}
