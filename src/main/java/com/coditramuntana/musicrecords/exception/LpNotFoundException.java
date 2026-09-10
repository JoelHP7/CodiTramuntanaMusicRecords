package com.coditramuntana.musicrecords.exception;

public class LpNotFoundException extends ResourceNotFoundException {

    public LpNotFoundException(Long id) {
        super("LP with id %d was not found".formatted(id));
    }

}
