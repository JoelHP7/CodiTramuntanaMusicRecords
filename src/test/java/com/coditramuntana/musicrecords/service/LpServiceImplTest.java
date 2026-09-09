package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.exception.ArtistNotFoundException;
import com.coditramuntana.musicrecords.exception.LpAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.LpNotFoundException;
import com.coditramuntana.musicrecords.exception.SongAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.SongNotFoundException;
import com.coditramuntana.musicrecords.mapper.LpMapper;
import com.coditramuntana.musicrecords.mapper.SongMapper;
import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.data.Song;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import com.coditramuntana.musicrecords.model.dto.SongRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.coditramuntana.musicrecords.repository.SongRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LpServiceImplTest {

    @Mock
    private LpRepository lpRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private AuthorService authorService;

    @Mock
    private LpMapper lpMapper;

    @Mock
    private SongMapper songMapper;

    @InjectMocks
    private LpServiceImpl lpService;

    // ==================== FILTERING ====================

    @Test
    @DisplayName("Should use the unfiltered query when no artist filter is provided")
    void getLps_shouldUseUnfilteredQueryWhenArtistParamIsBlank() {
        when(lpRepository.findAllWithArtist()).thenReturn(List.of());

        lpService.getLps("  ");

        verify(lpRepository).findAllWithArtist();
        verify(lpRepository, never()).findByArtistNameContaining(any());
    }

    @Test
    @DisplayName("Should filter in the database, not in memory, when an artist name is given")
    void getLps_shouldUseFilteredQueryWhenArtistParamIsProvided() {
        when(lpRepository.findByArtistNameContaining("meta")).thenReturn(List.of());

        lpService.getLps(" meta ");

        verify(lpRepository).findByArtistNameContaining("meta");
        verify(lpRepository, never()).findAllWithArtist();
    }

    // ==================== CREATION ====================

    @Test
    @DisplayName("Should fail when creating an LP for an artist that does not exist")
    void createLp_shouldThrowNotFoundWhenArtistDoesNotExist() {
        LpRequest request = LpRequest.builder().name("Ghost").artistId(99L).build();
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lpService.createLp(request))
                .isInstanceOf(ArtistNotFoundException.class);

        verify(lpRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject an LP whose name is already used by the same artist")
    void createLp_shouldThrowConflictWhenSameArtistAlreadyHasThatLp() {
        LpRequest request = LpRequest.builder().name("Black album").artistId(1L).build();
        Artist artist = Artist.builder().id(1L).name("Metallica").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(lpRepository.existsByArtistIdAndNameIgnoreCase(1L, "Black album")).thenReturn(true);

        assertThatThrownBy(() -> lpService.createLp(request))
                .isInstanceOf(LpAlreadyExistsException.class)
                .hasMessageContaining("Metallica");
    }

    @Test
    @DisplayName("Should allow two different artists to have an LP with the same name")
    void createLp_shouldAllowSameNameForDifferentArtist() {
        LpRequest request = LpRequest.builder().name("Greatest Hits").artistId(2L).build();
        Artist artist = Artist.builder().id(2L).name("Sepultura").build();
        Lp lp = Lp.builder().name("Greatest Hits").artist(artist).build();
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(lpRepository.existsByArtistIdAndNameIgnoreCase(2L, "Greatest Hits")).thenReturn(false);
        when(lpMapper.toEntity(request, artist)).thenReturn(lp);
        when(lpRepository.save(lp)).thenReturn(lp);

        lpService.createLp(request);

        verify(lpRepository).save(lp);
    }

    // ==================== SONGS ====================

    @Test
    @DisplayName("Should add a song to its LP delegating author resolution to the catalogue")
    void addSong_shouldResolveAuthorsAndAttachSongToLp() {
        Lp lp = Lp.builder().id(1L).name("Black album").build();
        SongRequest request = SongRequest.builder()
                .name("Sad But True")
                .authorNames(List.of("Hetfield"))
                .build();
        Author hetfield = Author.builder().id(1L).name("Hetfield").build();
        when(lpRepository.findById(1L)).thenReturn(Optional.of(lp));
        when(songRepository.existsByLpIdAndNameIgnoreCase(1L, "Sad But True")).thenReturn(false);
        when(authorService.resolveOrCreate(anyCollection())).thenReturn(Set.of(hetfield));
        when(songRepository.save(any(Song.class))).thenAnswer(invocation -> invocation.getArgument(0));

        lpService.addSong(1L, request);

        assertThat(lp.getSongs()).hasSize(1);
        assertThat(lp.getSongs().iterator().next().getAuthors()).containsExactly(hetfield);
        verify(authorService).resolveOrCreate(List.of("Hetfield"));
    }

    @Test
    @DisplayName("Should reject a song whose name is already present in the same LP")
    void addSong_shouldThrowConflictWhenLpAlreadyContainsThatSong() {
        Lp lp = Lp.builder().id(1L).name("Black album").build();
        SongRequest request = SongRequest.builder()
                .name("Enter Sandman")
                .authorNames(List.of("Hetfield"))
                .build();
        when(lpRepository.findById(1L)).thenReturn(Optional.of(lp));
        when(songRepository.existsByLpIdAndNameIgnoreCase(1L, "Enter Sandman")).thenReturn(true);

        assertThatThrownBy(() -> lpService.addSong(1L, request))
                .isInstanceOf(SongAlreadyExistsException.class);

        verify(songRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when adding a song to an LP that does not exist")
    void addSong_shouldThrowNotFoundWhenLpDoesNotExist() {
        SongRequest request = SongRequest.builder().name("X").authorNames(List.of("Y")).build();
        when(lpRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lpService.addSong(99L, request))
                .isInstanceOf(LpNotFoundException.class);
    }

    @Test
    @DisplayName("Should refuse to delete a song that belongs to a different LP")
    void deleteSong_shouldThrowNotFoundWhenSongBelongsToAnotherLp() {
        Lp requestedLp = Lp.builder().id(2L).name("Ride the Lightning").build();
        Lp otherLp = Lp.builder().id(1L).name("Black album").build();
        Song song = Song.builder().id(5L).name("Enter Sandman").lp(otherLp).build();
        when(lpRepository.findById(2L)).thenReturn(Optional.of(requestedLp));
        when(songRepository.findByIdWithAuthors(5L)).thenReturn(Optional.of(song));

        assertThatThrownBy(() -> lpService.deleteSong(2L, 5L))
                .isInstanceOf(SongNotFoundException.class);

        verify(lpRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should detach the song from its LP so orphan removal deletes it")
    void deleteSong_shouldRemoveSongFromItsLp() {
        Lp lp = Lp.builder().id(1L).name("Black album").build();
        Song song = Song.builder().id(5L).name("Enter Sandman").build();
        lp.addSong(song);
        when(lpRepository.findById(1L)).thenReturn(Optional.of(lp));
        when(songRepository.findByIdWithAuthors(5L)).thenReturn(Optional.of(song));

        lpService.deleteSong(1L, 5L);

        assertThat(lp.getSongs()).isEmpty();
        verify(lpRepository).save(lp);
    }

    @Test
    @DisplayName("Should replace the whole set of authors when a song is updated")
    void updateSong_shouldReplaceTheWholeAuthorSet() {
        Lp lp = Lp.builder().id(1L).name("Black album").build();
        Author hetfield = Author.builder().id(1L).name("Hetfield").build();
        Author ulrich = Author.builder().id(2L).name("Ulrich").build();
        Song song = Song.builder().id(5L).name("Enter Sandman").lp(lp).build();
        song.replaceAuthors(Set.of(hetfield));
        SongRequest request = SongRequest.builder()
                .name("Enter Sandman")
                .authorNames(List.of("Ulrich"))
                .build();
        when(lpRepository.findById(1L)).thenReturn(Optional.of(lp));
        when(songRepository.findByIdWithAuthors(5L)).thenReturn(Optional.of(song));
        when(songRepository.existsByLpIdAndNameIgnoreCaseAndIdNot(1L, "Enter Sandman", 5L)).thenReturn(false);
        when(authorService.resolveOrCreate(anyCollection())).thenReturn(Set.of(ulrich));

        lpService.updateSong(1L, 5L, request);

        assertThat(song.getAuthors()).containsExactly(ulrich);
    }

}
