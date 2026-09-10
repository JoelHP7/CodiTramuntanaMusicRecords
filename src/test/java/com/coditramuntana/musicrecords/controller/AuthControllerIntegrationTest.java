package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.data.Role;
import com.coditramuntana.musicrecords.model.data.UserAccount;
import com.coditramuntana.musicrecords.model.dto.LoginRequest;
import com.coditramuntana.musicrecords.model.dto.RefreshRequest;
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

import static com.coditramuntana.musicrecords.controller.AuthPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.LOGIN_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.LOGOUT_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.ME_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.REFRESH_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        userAccountRepository.save(UserAccount.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build());
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("POST login - Should return both tokens for valid credentials")
    void login_shouldReturnTokens() throws Exception {
        mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    @DisplayName("POST login - Should return 401 for a wrong password")
    void login_shouldReturn401ForWrongPassword() throws Exception {
        mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("admin", "nope"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                // The message must not reveal whether the user exists.
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST login - Should return the same 401 for an unknown user")
    void login_shouldReturn401ForUnknownUser() throws Exception {
        mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("ghost", "whatever"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST login - Should return 400 when the payload is incomplete")
    void login_shouldReturn400WhenUsernameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("", "admin123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST login - Should reject a disabled user")
    void login_shouldReturn401ForDisabledUser() throws Exception {
        UserAccount disabled = userAccountRepository.findByUsernameIgnoreCase("admin").orElseThrow();
        disabled.setEnabled(false);
        userAccountRepository.save(disabled);

        mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isUnauthorized());
    }

    // ==================== REFRESH ====================

    @Test
    @DisplayName("POST refresh - Should issue a new pair and revoke the used token")
    void refresh_shouldRotateTheToken() throws Exception {
        String refreshToken = loginAndReadField("refreshToken");

        mockMvc.perform(post(BASE_PATH + REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        // The rotated token is revoked, so replaying it must fail.
        mockMvc.perform(post(BASE_PATH + REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST refresh - Should return 401 for an unknown refresh token")
    void refresh_shouldReturn401ForUnknownToken() throws Exception {
        mockMvc.perform(post(BASE_PATH + REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest("made-up-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST refresh - Should return 401 for an expired refresh token")
    void refresh_shouldReturn401ForExpiredToken() throws Exception {
        String refreshToken = loginAndReadField("refreshToken");
        var stored = refreshTokenRepository.findByTokenWithUser(refreshToken).orElseThrow();
        stored.setExpiresAt(System.currentTimeMillis() - 1_000L);
        refreshTokenRepository.save(stored);

        mockMvc.perform(post(BASE_PATH + REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    // ==================== LOGOUT ====================

    @Test
    @DisplayName("POST logout - Should revoke the refresh token")
    void logout_shouldRevokeTheToken() throws Exception {
        String refreshToken = loginAndReadField("refreshToken");

        mockMvc.perform(post(BASE_PATH + LOGOUT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(refreshToken))))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByTokenWithUser(refreshToken).orElseThrow().isRevoked())
                .isTrue();

        mockMvc.perform(post(BASE_PATH + REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST logout - Should answer 204 even for an unknown token")
    void logout_shouldNotRevealWhetherTheTokenExisted() throws Exception {
        mockMvc.perform(post(BASE_PATH + LOGOUT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest("made-up-token"))))
                .andExpect(status().isNoContent());
    }

    // ==================== PROFILE ====================

    @Test
    @DisplayName("GET me - Should return the profile of the token owner")
    void me_shouldReturnCurrentUser() throws Exception {
        String accessToken = loginAndReadField("accessToken");

        mockMvc.perform(get(BASE_PATH + ME_PATH).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET me - Should return 401 without a token")
    void me_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get(BASE_PATH + ME_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private String loginAndReadField(String field) throws Exception {
        String response = mockMvc.perform(post(BASE_PATH + LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get(field).asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

}
