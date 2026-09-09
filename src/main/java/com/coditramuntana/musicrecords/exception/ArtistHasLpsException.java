package com.coditramuntana.musicrecords.exception;

/**
 * Thrown when deleting an artist that still owns LPs.
 *
 * <p>Deleting is rejected rather than cascaded: {@code lps.artist_id} is not nullable, so
 * the alternatives are cascading or refusing, and silently destroying a whole discography
 * is a poor default for a public API.
 */
public class ArtistHasLpsException extends ResourceConflictException {

    public ArtistHasLpsException(String artistName, long lpCount) {
        super("Artist '%s' cannot be deleted because it still has %d LP(s)".formatted(artistName, lpCount));
    }

}
