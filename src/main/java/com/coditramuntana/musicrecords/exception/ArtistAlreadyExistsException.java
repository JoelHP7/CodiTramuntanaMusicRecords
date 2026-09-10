package com.coditramuntana.musicrecords.exception;

public class ArtistAlreadyExistsException extends ResourceConflictException {

    public ArtistAlreadyExistsException(String name) {
        super("An artist named '%s' already exists".formatted(name));
    }

}
