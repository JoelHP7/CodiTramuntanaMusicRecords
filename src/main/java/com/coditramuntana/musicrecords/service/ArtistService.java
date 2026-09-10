package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.model.dto.ArtistDetailDto;
import com.coditramuntana.musicrecords.model.dto.ArtistDto;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import com.coditramuntana.musicrecords.model.dto.LpDto;

import java.util.List;

public interface ArtistService {

    List<ArtistDto> getArtists(String name);

    ArtistDetailDto getArtist(Long id);

    List<LpDto> getArtistLps(Long id);

    ArtistDto createArtist(ArtistRequest request);

    ArtistDto updateArtist(Long id, ArtistRequest request);

    void deleteArtist(Long id);

}
