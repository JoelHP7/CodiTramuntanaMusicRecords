package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.dto.SongRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.AuthorRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.coditramuntana.musicrecords.controller.ReportPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.ReportPaths.DISCOGRAPHY_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End to end check of the home page report, using the very example given in the
 * assignment: Metallica "Black album" with three songs, and Sepultura "Against" with one.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
class ReportControllerIntegrationTest {

    private static final String REPORT_PATH = BASE_PATH + DISCOGRAPHY_PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() throws Exception {
        lpRepository.deleteAll();
        artistRepository.deleteAll();
        authorRepository.deleteAll();

        Artist metallica = artistRepository.save(Artist.builder().name("Metallica").build());
        Artist sepultura = artistRepository.save(Artist.builder().name("Sepultura").build());
        Artist ironMaiden = artistRepository.save(Artist.builder().name("Iron Maiden").build());

        Lp blackAlbum = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        addSong(blackAlbum, "My Friend of Misery", "Hetfield", "Jason N.");
        addSong(blackAlbum, "Enter Sandman", "Hetfield");
        addSong(blackAlbum, "Unforgiven", "Hetfield", "Hammett");

        Lp against = lpRepository.save(Lp.builder().name("Against").artist(sepultura).build());
        addSong(against, "Hatred Aside", "Jason N.");

        lpRepository.save(Lp.builder().name("Piece of Mind").artist(ironMaiden).build());
    }

    @Test
    @DisplayName("GET - Should report the Black album with three songs and its three distinct authors")
    void getReport_shouldMatchTheExampleOfTheAssignment() throws Exception {
        mockMvc.perform(get(REPORT_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.lpName == 'Black album')].artistName").value("Metallica"))
                .andExpect(jsonPath("$[?(@.lpName == 'Black album')].songCount").value(3))
                .andExpect(jsonPath("$[?(@.lpName == 'Black album')].authors[0]").value("Hammett"))
                .andExpect(jsonPath("$[?(@.lpName == 'Black album')].authors[1]").value("Hetfield"))
                .andExpect(jsonPath("$[?(@.lpName == 'Black album')].authors[2]").value("Jason N."));
    }

    @Test
    @DisplayName("GET - Should report Against with a single song written by Jason N.")
    void getReport_shouldReportSepulturaRow() throws Exception {
        mockMvc.perform(get(REPORT_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.lpName == 'Against')].artistName").value("Sepultura"))
                .andExpect(jsonPath("$[?(@.lpName == 'Against')].songCount").value(1))
                .andExpect(jsonPath("$[?(@.lpName == 'Against')].authors[0]").value("Jason N."));
    }

    @Test
    @DisplayName("GET - Should keep an LP without songs in the report with zero songs")
    void getReport_shouldIncludeLpWithoutSongs() throws Exception {
        mockMvc.perform(get(REPORT_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.lpName == 'Piece of Mind')].songCount").value(0))
                .andExpect(jsonPath("$[?(@.lpName == 'Piece of Mind')].authors.length()").value(0));
    }

    @Test
    @DisplayName("GET - Should return one row per LP, ordered by artist name")
    void getReport_shouldReturnOneRowPerLpOrderedByArtist() throws Exception {
        mockMvc.perform(get(REPORT_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].artistName").value("Iron Maiden"))
                .andExpect(jsonPath("$[1].artistName").value("Metallica"))
                .andExpect(jsonPath("$[2].artistName").value("Sepultura"));
    }

    private void addSong(Lp lp, String name, String... authors) throws Exception {
        mockMvc.perform(post(LpPaths.BASE_PATH + "/{lpId}/songs", lp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SongRequest.builder()
                                .name(name)
                                .authorNames(List.of(authors))
                                .build())))
                .andExpect(status().isCreated());
    }

}
