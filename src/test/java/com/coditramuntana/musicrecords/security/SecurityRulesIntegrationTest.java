package com.coditramuntana.musicrecords.security;

import com.coditramuntana.musicrecords.controller.ArtistPaths;
import com.coditramuntana.musicrecords.controller.AuthPaths;
import com.coditramuntana.musicrecords.controller.LpPaths;
import com.coditramuntana.musicrecords.controller.ReportPaths;
import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Role;
import com.coditramuntana.musicrecords.model.data.UserAccount;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import com.coditramuntana.musicrecords.model.dto.LoginRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.coditramuntana.musicrecords.repository.RefreshTokenRepository;
import com.coditramuntana.musicrecords.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the access rules themselves, without {@code @WithMockUser}: every request here goes
 * through the real filter chain with a real token, which is what the rest of the integration
 * tests deliberately shortcut.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LpRepository lpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        lpRepository.deleteAll();
        artistRepository.deleteAll();

        userAccountRepository.save(UserAccount.builder()
                .username("editor")
                .password(passwordEncoder.encode("editor123"))
                .role(Role.USER)
                .enabled(true)
                .build());
    }

    // ==================== PUBLIC READS ====================

    @Test
    @DisplayName("Should let anonymous callers read the discography report")
    void report_shouldBePublic() throws Exception {
        mockMvc.perform(get(ReportPaths.BASE_PATH + ReportPaths.DISCOGRAPHY_PATH))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should let anonymous callers list artists and LPs")
    void listings_shouldBePublic() throws Exception {
        mockMvc.perform(get(ArtistPaths.BASE_PATH)).andExpect(status().isOk());
        mockMvc.perform(get(LpPaths.BASE_PATH)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should serve the frontend and the API documentation without a token")
    void staticResourcesAndDocs_shouldBePublic() throws Exception {
        mockMvc.perform(get("/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should answer 404, not 500, for an unknown path")
    void unknownPath_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/there-is-nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ==================== PROTECTED WRITES ====================

    @Test
    @DisplayName("Should reject an anonymous write with 401 and the standard error body")
    void write_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post(ArtistPaths.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("Ghost").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Should reject an anonymous delete with 401")
    void delete_shouldReturn401WithoutToken() throws Exception {
        Artist artist = artistRepository.save(Artist.builder().name("Metallica").build());

        mockMvc.perform(delete(ArtistPaths.BASE_PATH + "/{id}", artist.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should accept a write carrying a valid token")
    void write_shouldSucceedWithToken() throws Exception {
        String token = login();

        mockMvc.perform(post(ArtistPaths.BASE_PATH)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("Metallica").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Metallica"));
    }

    // ==================== TOKEN VALIDATION ====================

    @Test
    @DisplayName("Should reject a tampered token")
    void write_shouldReturn401WithTamperedToken() throws Exception {
        String token = login();
        String tampered = token.substring(0, token.length() - 3) + "abc";

        mockMvc.perform(post(ArtistPaths.BASE_PATH)
                        .header("Authorization", "Bearer " + tampered)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("Ghost").build())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should ignore an Authorization header that is not a Bearer token")
    void write_shouldReturn401WithNonBearerHeader() throws Exception {
        mockMvc.perform(post(ArtistPaths.BASE_PATH)
                        .header("Authorization", "Basic dXNlcjpwYXNz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("Ghost").build())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject a token whose user has been disabled after it was issued")
    void write_shouldReturn401WhenUserGetsDisabled() throws Exception {
        String token = login();

        UserAccount editor = userAccountRepository.findByUsernameIgnoreCase("editor").orElseThrow();
        editor.setEnabled(false);
        userAccountRepository.save(editor);

        mockMvc.perform(post(ArtistPaths.BASE_PATH)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ArtistRequest.builder().name("Ghost").build())))
                .andExpect(status().isUnauthorized());
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post(AuthPaths.BASE_PATH + AuthPaths.LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("editor", "editor123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

}
