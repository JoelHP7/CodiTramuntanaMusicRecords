package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import com.coditramuntana.musicrecords.model.dto.SongRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.AuthorRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.coditramuntana.musicrecords.repository.SongRepository;
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

import static com.coditramuntana.musicrecords.controller.LpPaths.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
class LpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Artist metallica;
    private Artist sepultura;

    @BeforeEach
    void setUp() {
        lpRepository.deleteAll();
        artistRepository.deleteAll();
        authorRepository.deleteAll();

        metallica = artistRepository.save(Artist.builder().name("Metallica").build());
        sepultura = artistRepository.save(Artist.builder().name("Sepultura").build());
    }

    // ==================== CREATION ====================

    @Test
    @DisplayName("POST - Should create an LP for an existing artist")
    void createLp_shouldReturn201WithArtistName() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LpRequest.builder()
                                .name("Black album")
                                .description("Fifth studio album")
                                .artistId(metallica.getId())
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.artistName").value("Metallica"));
    }

    @Test
    @DisplayName("POST - Should return 404 when the artist does not exist")
    void createLp_shouldReturn404WhenArtistIsUnknown() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LpRequest.builder().name("Ghost").artistId(9999L).build())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST - Should return 409 when the artist already has an LP with that name")
    void createLp_shouldReturn409ForDuplicatedNameWithinSameArtist() throws Exception {
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LpRequest.builder()
                                .name("black ALBUM")
                                .artistId(metallica.getId())
                                .build())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST - Should allow the same LP name for a different artist")
    void createLp_shouldAllowSameNameForAnotherArtist() throws Exception {
        lpRepository.save(Lp.builder().name("Greatest Hits").artist(metallica).build());

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LpRequest.builder()
                                .name("Greatest Hits")
                                .artistId(sepultura.getId())
                                .build())))
                .andExpect(status().isCreated());
    }

    // ==================== FILTERING ====================

    @Test
    @DisplayName("GET - Should filter LPs by a partial artist name")
    void getLps_shouldFilterByArtistName() throws Exception {
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        lpRepository.save(Lp.builder().name("Against").artist(sepultura).build());

        mockMvc.perform(get(BASE_PATH).param("artistName", "meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Black album"));
    }

    @Test
    @DisplayName("GET - Should return an empty list when no artist matches the filter")
    void getLps_shouldReturnEmptyListWhenFilterMatchesNothing() throws Exception {
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());

        mockMvc.perform(get(BASE_PATH).param("artistName", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET - Should return every LP when no filter is given")
    void getLps_shouldReturnAllWhenNoFilter() throws Exception {
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        lpRepository.save(Lp.builder().name("Against").artist(sepultura).build());

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== SONGS ====================

    @Test
    @DisplayName("POST - Should add a song reusing an existing author instead of duplicating it")
    void addSong_shouldReuseExistingAuthor() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());

        mockMvc.perform(post(BASE_PATH + "/{lpId}/songs", lp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SongRequest.builder()
                                .name("Enter Sandman")
                                .authorNames(List.of("Hetfield"))
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(post(BASE_PATH + "/{lpId}/songs", lp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SongRequest.builder()
                                .name("Unforgiven")
                                .authorNames(List.of("hetfield", "Hammett"))
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authors.length()").value(2));

        // Hetfield was requested twice with different casing and must exist only once.
        assertThat(authorRepository.findAllByOrderByNameAsc())
                .extracting("name")
                .containsExactly("Hammett", "Hetfield");
    }

    @Test
    @DisplayName("POST - Should return 400 when a song is sent without authors")
    void addSong_shouldReturn400WhenAuthorsAreMissing() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());

        mockMvc.perform(post(BASE_PATH + "/{lpId}/songs", lp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SongRequest.builder()
                                .name("Enter Sandman")
                                .authorNames(List.of())
                                .build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET - Should expose the songs of an LP with their authors")
    void getLp_shouldReturnSongsWithAuthors() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        addSong(lp, "Enter Sandman", "Hetfield");

        mockMvc.perform(get(BASE_PATH + "/{id}", lp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs.length()").value(1))
                .andExpect(jsonPath("$.songs[0].name").value("Enter Sandman"))
                .andExpect(jsonPath("$.songs[0].authors[0]").value("Hetfield"));
    }

    @Test
    @DisplayName("PUT - Should replace the authors of a song")
    void updateSong_shouldReplaceAuthors() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        Long songId = addSong(lp, "Enter Sandman", "Hetfield");

        mockMvc.perform(put(BASE_PATH + "/{lpId}/songs/{songId}", lp.getId(), songId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SongRequest.builder()
                                .name("Enter Sandman")
                                .authorNames(List.of("Ulrich"))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authors.length()").value(1))
                .andExpect(jsonPath("$.authors[0]").value("Ulrich"));
    }

    @Test
    @DisplayName("DELETE - Should remove the song but keep its authors in the catalogue")
    void deleteSong_shouldKeepAuthors() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        Long songId = addSong(lp, "Enter Sandman", "Hetfield");

        mockMvc.perform(delete(BASE_PATH + "/{lpId}/songs/{songId}", lp.getId(), songId))
                .andExpect(status().isNoContent());

        assertThat(songRepository.findById(songId)).isEmpty();
        assertThat(authorRepository.findAllByOrderByNameAsc()).hasSize(1);
    }

    @Test
    @DisplayName("DELETE - Should return 404 when the song belongs to a different LP")
    void deleteSong_shouldReturn404WhenSongBelongsToAnotherLp() throws Exception {
        Lp blackAlbum = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        Lp against = lpRepository.save(Lp.builder().name("Against").artist(sepultura).build());
        Long songId = addSong(blackAlbum, "Enter Sandman", "Hetfield");

        mockMvc.perform(delete(BASE_PATH + "/{lpId}/songs/{songId}", against.getId(), songId))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETION ====================

    @Test
    @DisplayName("DELETE - Should delete an LP together with its songs")
    void deleteLp_shouldCascadeToItsSongs() throws Exception {
        Lp lp = lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        Long songId = addSong(lp, "Enter Sandman", "Hetfield");

        mockMvc.perform(delete(BASE_PATH + "/{id}", lp.getId()))
                .andExpect(status().isNoContent());

        assertThat(lpRepository.findById(lp.getId())).isEmpty();
        assertThat(songRepository.findById(songId)).isEmpty();
    }

    private Long addSong(Lp lp, String name, String author) throws Exception {
        String response = mockMvc.perform(post(BASE_PATH + "/{lpId}/songs", lp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SongRequest.builder()
                                .name(name)
                                .authorNames(List.of(author))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

}
