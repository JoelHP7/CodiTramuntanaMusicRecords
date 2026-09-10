package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.dto.AuthResponse;
import com.coditramuntana.musicrecords.model.dto.LoginRequest;
import com.coditramuntana.musicrecords.model.dto.RefreshRequest;
import com.coditramuntana.musicrecords.model.dto.UserDto;
import com.coditramuntana.musicrecords.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.coditramuntana.musicrecords.controller.AuthPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.LOGIN_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.LOGOUT_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.ME_PATH;
import static com.coditramuntana.musicrecords.controller.AuthPaths.REFRESH_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtaining and renewing access tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Log in",
            description = "Validates the credentials and returns an access token plus a refresh token."
    )
    @ApiResponse(responseCode = "200", description = "Tokens issued")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping(value = LOGIN_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST {}{} - user='{}'", BASE_PATH, LOGIN_PATH, request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Renew the tokens",
            description = "Exchanges a valid refresh token for a new pair. The used token is revoked, "
                    + "so a leaked refresh token stops working as soon as its owner refreshes."
    )
    @ApiResponse(responseCode = "200", description = "New tokens issued")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "401", description = "The refresh token is unknown, revoked or expired")
    @PostMapping(value = REFRESH_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.info("POST {}{}", BASE_PATH, REFRESH_PATH);
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(
            summary = "Log out",
            description = "Revokes the given refresh token. Always answers 204, also when the token "
                    + "was already unknown, so it cannot be used to probe for valid tokens."
    )
    @ApiResponse(responseCode = "204", description = "Token revoked")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @PostMapping(value = LOGOUT_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        log.info("POST {}{}", BASE_PATH, LOGOUT_PATH);
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Current user",
            description = "Returns the profile of the user owning the access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Authenticated user")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @GetMapping(value = ME_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET {}{} - user='{}'", BASE_PATH, ME_PATH, userDetails.getUsername());
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }

}
