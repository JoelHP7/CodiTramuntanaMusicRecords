package com.coditramuntana.musicrecords.controller;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Route constants of the artists API.
 */
@NoArgsConstructor(access = PRIVATE)
public final class ArtistPaths {

    public static final String BASE_PATH = "/api/artists";
    public static final String ID_PATH = "/{id}";
    public static final String LPS_PATH = "/{id}/lps";

}
