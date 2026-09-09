package com.coditramuntana.musicrecords.exception;

/**
 * Thrown when an operation conflicts with the current state of the data. Mapped to HTTP 409.
 */
public abstract class ResourceConflictException extends MusicRecordsException {

    protected ResourceConflictException(String message) {
        super(message);
    }

}
