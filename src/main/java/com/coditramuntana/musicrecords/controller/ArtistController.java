package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.dto.ArtistDetailDto;
import com.coditramuntana.musicrecords.model.dto.ArtistDto;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.coditramuntana.musicrecords.controller.ArtistPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.ArtistPaths.ID_PATH;
import static com.coditramuntana.musicrecords.controller.ArtistPaths.LPS_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Management of the artists of the discography")
public class ArtistController {

    private final ArtistService artistService;

    @Operation(
            summary = "List artists",
            description = "Returns every artist, optionally filtered by a partial, case insensitive name."
    )
    @ApiResponse(responseCode = "200", description = "List of artists")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ArtistDto>> getArtists(
            @RequestParam(required = false) String name) {
        log.info("GET {} - name='{}'", BASE_PATH, name);
        List<ArtistDto> artists = artistService.getArtists(name);
        log.info("GET {} - returning {} artists", BASE_PATH, artists.size());
        return ResponseEntity.ok(artists);
    }

    @Operation(
            summary = "Get artist detail",
            description = "Returns an artist together with the total number of LPs it owns."
    )
    @ApiResponse(responseCode = "200", description = "Artist detail")
    @ApiResponse(responseCode = "404", description = "Artist not found")
    @GetMapping(value = ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistDetailDto> getArtist(@PathVariable Long id) {
        log.info("GET {}{} - id={}", BASE_PATH, ID_PATH, id);
        return ResponseEntity.ok(artistService.getArtist(id));
    }

    @Operation(
            summary = "List the LPs of an artist",
            description = "Returns every LP belonging to the given artist."
    )
    @ApiResponse(responseCode = "200", description = "List of LPs of the artist")
    @ApiResponse(responseCode = "404", description = "Artist not found")
    @GetMapping(value = LPS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LpDto>> getArtistLps(@PathVariable Long id) {
        log.info("GET {}{} - id={}", BASE_PATH, LPS_PATH, id);
        List<LpDto> lps = artistService.getArtistLps(id);
        log.info("GET {}{} - returning {} LPs", BASE_PATH, LPS_PATH, lps.size());
        return ResponseEntity.ok(lps);
    }

    @Operation(
            summary = "Create an artist",
            description = "Creates a new artist. Artist names are unique, ignoring case."
    )
    @ApiResponse(responseCode = "201", description = "Artist created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "409", description = "An artist with the same name already exists")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistDto> createArtist(@Valid @RequestBody ArtistRequest request) {
        log.info("POST {} - name='{}'", BASE_PATH, request.getName());
        ArtistDto created = artistService.createArtist(request);
        log.info("POST {} - artist created with id={}", BASE_PATH, created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an artist")
    @ApiResponse(responseCode = "200", description = "Artist updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Artist not found")
    @ApiResponse(responseCode = "409", description = "Another artist with the same name already exists")
    @PutMapping(value = ID_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistDto> updateArtist(@PathVariable Long id,
                                                  @Valid @RequestBody ArtistRequest request) {
        log.info("PUT {}{} - id={}", BASE_PATH, ID_PATH, id);
        return ResponseEntity.ok(artistService.updateArtist(id, request));
    }

    @Operation(
            summary = "Delete an artist",
            description = "Deletes an artist. The operation is rejected while the artist still owns LPs, "
                    + "so a whole discography can never be removed by accident."
    )
    @ApiResponse(responseCode = "204", description = "Artist deleted")
    @ApiResponse(responseCode = "404", description = "Artist not found")
    @ApiResponse(responseCode = "409", description = "The artist still owns LPs")
    @DeleteMapping(ID_PATH)
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        log.info("DELETE {}{} - id={}", BASE_PATH, ID_PATH, id);
        artistService.deleteArtist(id);
        log.info("DELETE {}{} - artist id={} deleted", BASE_PATH, ID_PATH, id);
        return ResponseEntity.noContent().build();
    }

}
