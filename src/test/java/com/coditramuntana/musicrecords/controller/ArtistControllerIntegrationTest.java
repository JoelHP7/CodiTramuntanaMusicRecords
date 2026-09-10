package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.coditramuntana.musicrecords.controller.ArtistPaths.BASE_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtistControllerIntegrationTest {

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
    void setUp() {
        lpRepository.deleteAll();
        artistRepository.deleteAll();
        authorRepository.deleteAll();
    }

    // ==================== CREATION ====================

    @Test
    @DisplayName("POST - Should create an artist and return 201 with its identifier")
    void createArtist_shouldReturn201WithId() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder()
                                .name("Metallica")
                                .description("American heavy metal band")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Metallica"));
    }

    @Test
    @DisplayName("POST - Should return 400 when the name is blank")
    void createArtist_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("  ").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }

    @Test
    @DisplayName("POST - Should return 409 when the name already exists ignoring case")
    void createArtist_shouldReturn409WhenNameIsDuplicated() throws Exception {
        artistRepository.save(Artist.builder().name("Metallica").build());

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("metallica").build())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ==================== READING ====================

    @Test
    @DisplayName("GET - Should return 404 for an artist that does not exist")
    void getArtist_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET - Should expose the total number of LPs in the artist detail")
    void getArtist_shouldReturnLpCount() throws Exception {
        Artist metallica = artistRepository.save(Artist.builder().name("Metallica").build());
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        lpRepository.save(Lp.builder().name("Ride the Lightning").artist(metallica).build());

        mockMvc.perform(get(BASE_PATH + "/{id}", metallica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Metallica"))
                .andExpect(jsonPath("$.lpCount").value(2));
    }

    @Test
    @DisplayName("GET - Should list the LPs that belong to the artist")
    void getArtistLps_shouldReturnOnlyItsOwnLps() throws Exception {
        Artist metallica = artistRepository.save(Artist.builder().name("Metallica").build());
        Artist sepultura = artistRepository.save(Artist.builder().name("Sepultura").build());
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());
        lpRepository.save(Lp.builder().name("Against").artist(sepultura).build());

        mockMvc.perform(get(BASE_PATH + "/{id}/lps", metallica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Black album"));
    }

    @Test
    @DisplayName("GET - Should filter artists by a partial name")
    void getArtists_shouldFilterByName() throws Exception {
        artistRepository.save(Artist.builder().name("Metallica").build());
        artistRepository.save(Artist.builder().name("Sepultura").build());

        mockMvc.perform(get(BASE_PATH).param("name", "meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Metallica"));
    }

    // ==================== UPDATING ====================

    @Test
    @DisplayName("PUT - Should update an existing artist")
    void updateArtist_shouldReturn200WithUpdatedValues() throws Exception {
        Artist artist = artistRepository.save(Artist.builder().name("Metalica").build());

        mockMvc.perform(put(BASE_PATH + "/{id}", artist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder()
                                .name("Metallica")
                                .description("Fixed name")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Metallica"))
                .andExpect(jsonPath("$.description").value("Fixed name"));
    }

    // ==================== DELETION ====================

    @Test
    @DisplayName("DELETE - Should return 409 while the artist still owns LPs")
    void deleteArtist_shouldReturn409WhenArtistHasLps() throws Exception {
        Artist metallica = artistRepository.save(Artist.builder().name("Metallica").build());
        lpRepository.save(Lp.builder().name("Black album").artist(metallica).build());

        mockMvc.perform(delete(BASE_PATH + "/{id}", metallica.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Artist 'Metallica' cannot be deleted because it still has 1 LP(s)"));
    }

    @Test
    @DisplayName("DELETE - Should return 204 when the artist owns no LPs")
    void deleteArtist_shouldReturn204WhenArtistHasNoLps() throws Exception {
        Artist radiohead = artistRepository.save(Artist.builder().name("Radiohead").build());

        mockMvc.perform(delete(BASE_PATH + "/{id}", radiohead.getId()))
                .andExpect(status().isNoContent());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

}
