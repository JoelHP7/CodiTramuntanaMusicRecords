package com.coditramuntana.musicrecords.exception;

/**
 * Thrown when a login attempt does not match any active user. Mapped to HTTP 401.
 *
 * <p>The message is deliberately vague: telling the caller whether it was the user name or
 * the password that was wrong would help enumerate valid accounts.
 */
public class InvalidCredentialsException extends MusicRecordsException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }

}
