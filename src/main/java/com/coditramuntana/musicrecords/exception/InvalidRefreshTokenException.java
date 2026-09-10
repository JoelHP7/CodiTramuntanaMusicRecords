package com.coditramuntana.musicrecords.exception;

/**
 * Thrown when a refresh token is unknown, already revoked or expired. Mapped to HTTP 401.
 */
public class InvalidRefreshTokenException extends MusicRecordsException {

    public InvalidRefreshTokenException() {
        super("The refresh token is invalid or has expired");
    }

}
