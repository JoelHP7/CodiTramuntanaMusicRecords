package com.coditramuntana.musicrecords.exception;

public class SongNotFoundException extends ResourceNotFoundException {

    public SongNotFoundException(Long songId, Long lpId) {
        super("Song with id %d was not found in LP with id %d".formatted(songId, lpId));
    }

}
