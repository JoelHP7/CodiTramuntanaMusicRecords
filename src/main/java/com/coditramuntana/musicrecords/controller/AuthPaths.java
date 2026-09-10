package com.coditramuntana.musicrecords.controller;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Route constants of the authentication API.
 */
@NoArgsConstructor(access = PRIVATE)
public final class AuthPaths {

    public static final String BASE_PATH = "/api/auth";
    public static final String LOGIN_PATH = "/login";
    public static final String REFRESH_PATH = "/refresh";
    public static final String LOGOUT_PATH = "/logout";
    public static final String ME_PATH = "/me";

}
