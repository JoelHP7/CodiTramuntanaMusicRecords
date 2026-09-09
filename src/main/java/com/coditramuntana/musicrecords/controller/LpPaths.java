package com.coditramuntana.musicrecords.controller;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Route constants of the LPs API.
 */
@NoArgsConstructor(access = PRIVATE)
public final class LpPaths {

    public static final String BASE_PATH = "/api/lps";
    public static final String ID_PATH = "/{id}";
    public static final String SONGS_PATH = "/{lpId}/songs";
    public static final String SONG_ID_PATH = "/{lpId}/songs/{songId}";

}
