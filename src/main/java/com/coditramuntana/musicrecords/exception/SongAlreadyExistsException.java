package com.coditramuntana.musicrecords.exception;

public class SongAlreadyExistsException extends ResourceConflictException {

    public SongAlreadyExistsException(String lpName, String songName) {
        super("LP '%s' already contains a song named '%s'".formatted(lpName, songName));
    }

}
