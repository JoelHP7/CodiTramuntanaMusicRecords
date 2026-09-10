package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.dto.AuthorDto;
import com.coditramuntana.musicrecords.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.coditramuntana.musicrecords.controller.AuthorPaths.BASE_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Read only catalogue of song authors")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(
            summary = "List authors",
            description = "Returns the shared catalogue of authors, used to reuse existing authors "
                    + "when adding songs instead of creating duplicates."
    )
    @ApiResponse(responseCode = "200", description = "List of authors")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuthorDto>> getAuthors() {
        log.info("GET {}", BASE_PATH);
        List<AuthorDto> authors = authorService.getAllAuthors();
        log.info("GET {} - returning {} authors", BASE_PATH, authors.size());
        return ResponseEntity.ok(authors);
    }

}
