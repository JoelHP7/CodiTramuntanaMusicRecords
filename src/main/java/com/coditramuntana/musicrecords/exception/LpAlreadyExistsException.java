package com.coditramuntana.musicrecords.exception;

public class LpAlreadyExistsException extends ResourceConflictException {

    public LpAlreadyExistsException(String artistName, String lpName) {
        super("Artist '%s' already has an LP named '%s'".formatted(artistName, lpName));
    }

}
