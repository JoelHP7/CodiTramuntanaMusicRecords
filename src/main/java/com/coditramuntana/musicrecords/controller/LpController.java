package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.dto.LpDetailDto;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import com.coditramuntana.musicrecords.model.dto.SongDto;
import com.coditramuntana.musicrecords.model.dto.SongRequest;
import com.coditramuntana.musicrecords.service.LpService;
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

import static com.coditramuntana.musicrecords.controller.LpPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.LpPaths.ID_PATH;
import static com.coditramuntana.musicrecords.controller.LpPaths.SONGS_PATH;
import static com.coditramuntana.musicrecords.controller.LpPaths.SONG_ID_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "LPs", description = "Management of LPs and of the songs they contain")
public class LpController {

    private final LpService lpService;

    @Operation(
            summary = "List LPs",
            description = "Returns every LP, optionally filtered by a partial, case insensitive artist name."
    )
    @ApiResponse(responseCode = "200", description = "List of LPs")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LpDto>> getLps(
            @RequestParam(required = false) String artistName) {
        log.info("GET {} - artistName='{}'", BASE_PATH, artistName);
        List<LpDto> lps = lpService.getLps(artistName);
        log.info("GET {} - returning {} LPs", BASE_PATH, lps.size());
        return ResponseEntity.ok(lps);
    }

    @Operation(
            summary = "Get LP detail",
            description = "Returns an LP with its songs and the authors of each song."
    )
    @ApiResponse(responseCode = "200", description = "LP detail")
    @ApiResponse(responseCode = "404", description = "LP not found")
    @GetMapping(value = ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LpDetailDto> getLp(@PathVariable Long id) {
        log.info("GET {}{} - id={}", BASE_PATH, ID_PATH, id);
        return ResponseEntity.ok(lpService.getLp(id));
    }

    @Operation(
            summary = "Create an LP",
            description = "Creates an LP for an existing artist. An artist cannot have two LPs with the same name."
    )
    @ApiResponse(responseCode = "201", description = "LP created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Artist not found")
    @ApiResponse(responseCode = "409", description = "The artist already has an LP with the same name")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LpDto> createLp(@Valid @RequestBody LpRequest request) {
        log.info("POST {} - name='{}', artistId={}", BASE_PATH, request.getName(), request.getArtistId());
        LpDto created = lpService.createLp(request);
        log.info("POST {} - LP created with id={}", BASE_PATH, created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an LP")
    @ApiResponse(responseCode = "200", description = "LP updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "LP or artist not found")
    @ApiResponse(responseCode = "409", description = "The artist already has another LP with the same name")
    @PutMapping(value = ID_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LpDto> updateLp(@PathVariable Long id,
                                          @Valid @RequestBody LpRequest request) {
        log.info("PUT {}{} - id={}", BASE_PATH, ID_PATH, id);
        return ResponseEntity.ok(lpService.updateLp(id, request));
    }

    @Operation(
            summary = "Delete an LP",
            description = "Deletes an LP and the songs it contains. Authors are kept, since they are a shared catalogue."
    )
    @ApiResponse(responseCode = "204", description = "LP deleted")
    @ApiResponse(responseCode = "404", description = "LP not found")
    @DeleteMapping(ID_PATH)
    public ResponseEntity<Void> deleteLp(@PathVariable Long id) {
        log.info("DELETE {}{} - id={}", BASE_PATH, ID_PATH, id);
        lpService.deleteLp(id);
        log.info("DELETE {}{} - LP id={} deleted", BASE_PATH, ID_PATH, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add a song to an LP",
            description = "Adds a song and links it to its authors. Existing authors are reused by name, "
                    + "and missing ones are created."
    )
    @ApiResponse(responseCode = "201", description = "Song added")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "LP not found")
    @ApiResponse(responseCode = "409", description = "The LP already contains a song with the same name")
    @PostMapping(value = SONGS_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDto> addSong(@PathVariable Long lpId,
                                           @Valid @RequestBody SongRequest request) {
        log.info("POST {}{} - lpId={}, name='{}'", BASE_PATH, SONGS_PATH, lpId, request.getName());
        SongDto created = lpService.addSong(lpId, request);
        log.info("POST {}{} - song created with id={}", BASE_PATH, SONGS_PATH, created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Update a song of an LP",
            description = "Renames the song and replaces the whole set of its authors."
    )
    @ApiResponse(responseCode = "200", description = "Song updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "LP not found, or the song does not belong to it")
    @ApiResponse(responseCode = "409", description = "The LP already contains another song with the same name")
    @PutMapping(value = SONG_ID_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDto> updateSong(@PathVariable Long lpId,
                                              @PathVariable Long songId,
                                              @Valid @RequestBody SongRequest request) {
        log.info("PUT {}{} - lpId={}, songId={}", BASE_PATH, SONG_ID_PATH, lpId, songId);
        return ResponseEntity.ok(lpService.updateSong(lpId, songId, request));
    }

    @Operation(
            summary = "Remove a song from an LP",
            description = "Deletes the song. Its authors are kept in the catalogue."
    )
    @ApiResponse(responseCode = "204", description = "Song deleted")
    @ApiResponse(responseCode = "404", description = "LP not found, or the song does not belong to it")
    @DeleteMapping(SONG_ID_PATH)
    public ResponseEntity<Void> deleteSong(@PathVariable Long lpId,
                                           @PathVariable Long songId) {
        log.info("DELETE {}{} - lpId={}, songId={}", BASE_PATH, SONG_ID_PATH, lpId, songId);
        lpService.deleteSong(lpId, songId);
        log.info("DELETE {}{} - song id={} deleted", BASE_PATH, SONG_ID_PATH, songId);
        return ResponseEntity.noContent().build();
    }

}
