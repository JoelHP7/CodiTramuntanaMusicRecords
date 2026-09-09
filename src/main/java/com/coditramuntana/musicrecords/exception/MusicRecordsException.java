package com.coditramuntana.musicrecords.exception;

/**
 * Base class for every business exception of the application.
 */
public abstract class MusicRecordsException extends RuntimeException {

    protected MusicRecordsException(String message) {
        super(message);
    }

}
