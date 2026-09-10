package com.coditramuntana.musicrecords.exception;

/**
 * Thrown when a requested resource does not exist. Mapped to HTTP 404.
 */
public abstract class ResourceNotFoundException extends MusicRecordsException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }

}
