package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.exception.ArtistAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.ArtistHasLpsException;
import com.coditramuntana.musicrecords.exception.ArtistNotFoundException;
import com.coditramuntana.musicrecords.mapper.ArtistMapper;
import com.coditramuntana.musicrecords.mapper.LpMapper;
import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.dto.ArtistDetailDto;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private LpRepository lpRepository;

    @Mock
    private ArtistMapper artistMapper;

    @Mock
    private LpMapper lpMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    // ==================== CREATION ====================

    @Test
    @DisplayName("Should reject a new artist whose name already exists ignoring case")
    void createArtist_shouldThrowConflictWhenNameAlreadyExistsIgnoringCase() {
        ArtistRequest request = ArtistRequest.builder().name("metallica").build();
        when(artistRepository.existsByNameIgnoreCase("metallica")).thenReturn(true);

        assertThatThrownBy(() -> artistService.createArtist(request))
                .isInstanceOf(ArtistAlreadyExistsException.class)
                .hasMessageContaining("metallica");

        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should trim the name before checking whether it is already taken")
    void createArtist_shouldValidateTheTrimmedName() {
        ArtistRequest request = ArtistRequest.builder().name("  Metallica  ").build();
        Artist entity = Artist.builder().name("Metallica").build();
        when(artistRepository.existsByNameIgnoreCase("Metallica")).thenReturn(false);
        when(artistMapper.toEntity(request)).thenReturn(entity);
        when(artistRepository.save(entity)).thenReturn(entity);

        artistService.createArtist(request);

        verify(artistRepository).existsByNameIgnoreCase("Metallica");
    }

    // ==================== READING ====================

    @Test
    @DisplayName("Should fail when the requested artist does not exist")
    void getArtist_shouldThrowNotFoundWhenIdDoesNotExist() {
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.getArtist(99L))
                .isInstanceOf(ArtistNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should read the LP count with a count query, not by loading the collection")
    void getArtist_shouldIncludeLpCountFromCountQuery() {
        Artist artist = Artist.builder().id(1L).name("Metallica").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(lpRepository.countByArtistId(1L)).thenReturn(2L);
        when(artistMapper.toDetailDto(artist, 2L))
                .thenReturn(ArtistDetailDto.builder().id(1L).name("Metallica").lpCount(2L).build());

        ArtistDetailDto detail = artistService.getArtist(1L);

        assertThat(detail.getLpCount()).isEqualTo(2L);
        verify(lpRepository).countByArtistId(1L);
    }

    // ==================== UPDATING ====================

    @Test
    @DisplayName("Should let an artist keep its own name when being updated")
    void updateArtist_shouldAllowKeepingItsOwnName() {
        ArtistRequest request = ArtistRequest.builder().name("Metallica").build();
        Artist existing = Artist.builder().id(1L).name("Metallica").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(artistRepository.existsByNameIgnoreCaseAndIdNot("Metallica", 1L)).thenReturn(false);
        when(artistRepository.save(existing)).thenReturn(existing);

        artistService.updateArtist(1L, request);

        verify(artistRepository).save(existing);
    }

    @Test
    @DisplayName("Should reject an update that takes the name of another artist")
    void updateArtist_shouldThrowConflictWhenNameBelongsToAnotherArtist() {
        ArtistRequest request = ArtistRequest.builder().name("Sepultura").build();
        Artist existing = Artist.builder().id(1L).name("Metallica").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(artistRepository.existsByNameIgnoreCaseAndIdNot("Sepultura", 1L)).thenReturn(true);

        assertThatThrownBy(() -> artistService.updateArtist(1L, request))
                .isInstanceOf(ArtistAlreadyExistsException.class);

        verify(artistRepository, never()).save(any());
    }

    // ==================== DELETION ====================

    @Test
    @DisplayName("Should refuse to delete an artist that still owns LPs")
    void deleteArtist_shouldThrowConflictWhenArtistHasLps() {
        Artist artist = Artist.builder().id(1L).name("Metallica").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(lpRepository.countByArtistId(1L)).thenReturn(2L);

        assertThatThrownBy(() -> artistService.deleteArtist(1L))
                .isInstanceOf(ArtistHasLpsException.class)
                .hasMessageContaining("Metallica")
                .hasMessageContaining("2");

        verify(artistRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete an artist that owns no LPs")
    void deleteArtist_shouldDeleteWhenArtistHasNoLps() {
        Artist artist = Artist.builder().id(6L).name("Radiohead").build();
        when(artistRepository.findById(6L)).thenReturn(Optional.of(artist));
        when(lpRepository.countByArtistId(6L)).thenReturn(0L);

        artistService.deleteArtist(6L);

        verify(artistRepository).delete(artist);
    }

    // ==================== FILTERING ====================

    @Test
    @DisplayName("Should use the unfiltered query when no name filter is provided")
    void getArtists_shouldUseUnfilteredQueryWhenNameIsBlank() {
        when(artistRepository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of());

        artistService.getArtists("   ");

        verify(artistRepository).findAllByOrderByNameAsc();
        verify(artistRepository, never()).findByNameContainingIgnoreCaseOrderByName(any());
    }

    @Test
    @DisplayName("Should use the filtered query when a name filter is provided")
    void getArtists_shouldUseFilteredQueryWhenNameIsProvided() {
        when(artistRepository.findByNameContainingIgnoreCaseOrderByName("meta")).thenReturn(java.util.List.of());

        artistService.getArtists(" meta ");

        verify(artistRepository).findByNameContainingIgnoreCaseOrderByName("meta");
        verify(artistRepository, never()).findAllByOrderByNameAsc();
    }

}
