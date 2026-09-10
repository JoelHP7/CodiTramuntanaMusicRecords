package com.coditramuntana.musicrecords.config;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.dto.LpDetailDto;
import com.coditramuntana.musicrecords.model.dto.SongDto;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.AuthorRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.coditramuntana.musicrecords.service.LpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The seeder is enabled explicitly here, since it is switched off for the rest of the
 * test suite so every test controls its own data.
 */
@SpringBootTest
@TestPropertySource(properties = "app.seeding.enabled=true")
class DataSeederTest {

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LpService lpService;

    @BeforeEach
    void setUp() {
        lpRepository.deleteAll();
        artistRepository.deleteAll();
        authorRepository.deleteAll();
        dataSeeder.run(new DefaultApplicationArguments());
    }

    @Test
    @DisplayName("Should seed at least the five artists required by the assignment")
    void run_shouldSeedAtLeastFiveArtists() {
        assertThat(artistRepository.count()).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @DisplayName("Should seed the Metallica example with its three songs and authors")
    void run_shouldSeedTheMetallicaExample() {
        Artist metallica = artistRepository.findByNameIgnoreCase("Metallica").orElseThrow();
        LpDetailDto blackAlbum = blackAlbumOf(metallica);

        assertThat(blackAlbum.getSongs()).extracting(SongDto::getName)
                .containsExactlyInAnyOrder("My Friend of Misery", "Enter Sandman", "Unforgiven");

        assertThat(songNamed(blackAlbum, "My Friend of Misery").getAuthors())
                .containsExactly("Hetfield", "Jason N.");
        assertThat(songNamed(blackAlbum, "Enter Sandman").getAuthors())
                .containsExactly("Hetfield");
        assertThat(songNamed(blackAlbum, "Unforgiven").getAuthors())
                .containsExactly("Hammett", "Hetfield");
    }

    @Test
    @DisplayName("Should seed the Sepultura example sharing an author with Metallica")
    void run_shouldSeedTheSepulturaExampleSharingAnAuthor() {
        Artist sepultura = artistRepository.findByNameIgnoreCase("Sepultura").orElseThrow();
        LpDetailDto against = lpService.getLp(
                lpRepository.findByArtistId(sepultura.getId()).getFirst().getId());

        assertThat(against.getName()).isEqualTo("Against");
        assertThat(songNamed(against, "Hatred Aside").getAuthors()).containsExactly("Jason N.");

        // The very same author instance is reused across two different artists.
        assertThat(authorRepository.findAllByOrderByNameAsc())
                .filteredOn(author -> author.getName().equals("Jason N."))
                .hasSize(1);
    }

    @Test
    @DisplayName("Should not duplicate anything when the seeder runs a second time")
    void run_shouldBeIdempotent() {
        long artists = artistRepository.count();
        long lps = lpRepository.count();
        long authors = authorRepository.count();

        dataSeeder.run(new DefaultApplicationArguments());

        assertThat(artistRepository.count()).isEqualTo(artists);
        assertThat(lpRepository.count()).isEqualTo(lps);
        assertThat(authorRepository.count()).isEqualTo(authors);
    }

    private LpDetailDto blackAlbumOf(Artist artist) {
        return lpRepository.findByArtistId(artist.getId()).stream()
                .filter(lp -> lp.getName().equals("Black album"))
                .findFirst()
                .map(lp -> lpService.getLp(lp.getId()))
                .orElseThrow();
    }

    private SongDto songNamed(LpDetailDto lp, String name) {
        return lp.getSongs().stream()
                .filter(song -> song.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

}
