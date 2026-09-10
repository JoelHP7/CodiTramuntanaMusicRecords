package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the "filter LPs by artist name" requirement against a real SQLite database,
 * including that the filtering happens in SQL and that reading the artist name of each
 * row does not trigger extra queries.
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LpRepositoryFilterTest {

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private EntityManager entityManager;

    private Artist metallica;

    @BeforeEach
    void setUp() {
        metallica = persist(Artist.builder().name("Metallica").build());
        Artist sepultura = persist(Artist.builder().name("Sepultura").build());
        Artist radiohead = persist(Artist.builder().name("Radiohead").build());

        persist(Lp.builder().name("Black album").artist(metallica).build());
        persist(Lp.builder().name("Ride the Lightning").artist(metallica).build());
        persist(Lp.builder().name("Against").artist(sepultura).build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should match the artist name ignoring case")
    void findByArtistNameContaining_shouldMatchIgnoringCase() {
        List<Lp> lps = lpRepository.findByArtistNameContaining("meta");

        assertThat(lps).extracting(Lp::getName)
                .containsExactly("Black album", "Ride the Lightning");
    }

    @Test
    @DisplayName("Should match a fragment in the middle of the artist name")
    void findByArtistNameContaining_shouldMatchPartialName() {
        List<Lp> lps = lpRepository.findByArtistNameContaining("llic");

        assertThat(lps).hasSize(2);
    }

    @Test
    @DisplayName("Should return no LPs when the filter matches no artist")
    void findByArtistNameContaining_shouldReturnEmptyWhenNoMatch() {
        assertThat(lpRepository.findByArtistNameContaining("zzz")).isEmpty();
    }

    @Test
    @DisplayName("Should read the artist name of every LP without issuing extra queries")
    void findAllWithArtist_shouldNotTriggerAdditionalQueries() {
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.clear();

        List<Lp> lps = lpRepository.findAllWithArtist();
        lps.forEach(lp -> lp.getArtist().getName());

        assertThat(lps).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should count zero LPs for an artist that owns none")
    void countByArtistId_shouldReturnZeroForArtistWithoutLps() {
        Artist radiohead = entityManager.createQuery(
                        "select a from Artist a where a.name = 'Radiohead'", Artist.class)
                .getSingleResult();

        assertThat(lpRepository.countByArtistId(radiohead.getId())).isZero();
    }

    @Test
    @DisplayName("Should count every LP of an artist that owns several")
    void countByArtistId_shouldCountAllLpsOfTheArtist() {
        assertThat(lpRepository.countByArtistId(metallica.getId())).isEqualTo(2L);
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

}
