package com.coditramuntana.musicrecords.exception;

public class ArtistNotFoundException extends ResourceNotFoundException {

    public ArtistNotFoundException(Long id) {
        super("Artist with id %d was not found".formatted(id));
    }

}
