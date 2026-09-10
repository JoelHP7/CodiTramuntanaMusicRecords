package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.data.Song;
import com.coditramuntana.musicrecords.model.projection.LpAuthorRow;
import com.coditramuntana.musicrecords.model.projection.LpReportRow;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the two aggregated queries of the discography report against a real SQLite
 * database, which is the only way to prove that the JPQL translates into valid SQL for
 * the community dialect and returns the expected values.
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LpRepositoryReportTest {

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private EntityManager entityManager;

    private Author hetfield;
    private Author hammett;
    private Author jason;

    @BeforeEach
    void setUp() {
        hetfield = persist(Author.builder().name("Hetfield").build());
        hammett = persist(Author.builder().name("Hammett").build());
        jason = persist(Author.builder().name("Jason N.").build());

        Artist metallica = persist(Artist.builder().name("Metallica").build());
        Artist sepultura = persist(Artist.builder().name("Sepultura").build());
        Artist ironMaiden = persist(Artist.builder().name("Iron Maiden").build());

        // The example of the assignment: three songs, Hetfield writing all of them.
        Lp blackAlbum = Lp.builder().name("Black album").artist(metallica).build();
        addSong(blackAlbum, "My Friend of Misery", hetfield, jason);
        addSong(blackAlbum, "Enter Sandman", hetfield);
        addSong(blackAlbum, "Unforgiven", hetfield, hammett);
        persist(blackAlbum);

        Lp against = Lp.builder().name("Against").artist(sepultura).build();
        addSong(against, "Hatred Aside", jason);
        persist(against);

        // An LP without songs, which must still show up in the report.
        persist(Lp.builder().name("Piece of Mind").artist(ironMaiden).build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should return one row per LP, including LPs without songs")
    void findReportRows_shouldReturnOneRowPerLpIncludingLpsWithoutSongs() {
        List<LpReportRow> rows = lpRepository.findReportRows();

        assertThat(rows).extracting(LpReportRow::getLpName)
                .containsExactly("Piece of Mind", "Black album", "Against");
    }

    @Test
    @DisplayName("Should count zero songs for an LP that has none")
    void findReportRows_shouldCountZeroForLpWithoutSongs() {
        LpReportRow pieceOfMind = rowFor("Piece of Mind");

        assertThat(pieceOfMind.getSongCount()).isZero();
    }

    @Test
    @DisplayName("Should not multiply the song count when songs have several authors")
    void findReportRows_shouldNotMultiplyCountWhenSongsHaveMultipleAuthors() {
        LpReportRow blackAlbum = rowFor("Black album");

        assertThat(blackAlbum.getSongCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should order the report by artist name and then by LP name")
    void findReportRows_shouldBeOrderedByArtistThenLpName() {
        List<LpReportRow> rows = lpRepository.findReportRows();

        assertThat(rows).extracting(LpReportRow::getArtistName)
                .containsExactly("Iron Maiden", "Metallica", "Sepultura");
    }

    @Test
    @DisplayName("Should list the distinct authors of each LP in alphabetical order")
    void findReportAuthorRows_shouldReturnDistinctAuthorsPerLpAlphabetically() {
        List<LpAuthorRow> rows = lpRepository.findReportAuthorRows();
        Long blackAlbumId = rowFor("Black album").getLpId();

        List<String> authors = rows.stream()
                .filter(row -> row.getLpId().equals(blackAlbumId))
                .map(LpAuthorRow::getAuthorName)
                .toList();

        // Hetfield wrote three of the songs and must still appear exactly once.
        assertThat(authors).containsExactly("Hammett", "Hetfield", "Jason N.");
    }

    @Test
    @DisplayName("Should build the whole report with exactly two statements, never one per LP")
    void report_shouldExecuteExactlyTwoStatements() {
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.clear();

        lpRepository.findReportRows();
        lpRepository.findReportAuthorRows();

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);
    }

    private LpReportRow rowFor(String lpName) {
        return lpRepository.findReportRows().stream()
                .filter(row -> row.getLpName().equals(lpName))
                .findFirst()
                .orElseThrow();
    }

    private void addSong(Lp lp, String name, Author... authors) {
        Song song = Song.builder().name(name).build();
        song.replaceAuthors(Set.of(authors));
        lp.addSong(song);
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

}
