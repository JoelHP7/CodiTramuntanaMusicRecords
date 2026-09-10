package com.coditramuntana.musicrecords.mapper;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.dto.ArtistDetailDto;
import com.coditramuntana.musicrecords.model.dto.ArtistDto;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public Artist toEntity(ArtistRequest request) {
        return Artist.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
    }

    public ArtistDto toDto(Artist artist) {
        return ArtistDto.builder()
                .id(artist.getId())
                .name(artist.getName())
                .description(artist.getDescription())
                .build();
    }

    public ArtistDetailDto toDetailDto(Artist artist, long lpCount) {
        return ArtistDetailDto.builder()
                .id(artist.getId())
                .name(artist.getName())
                .description(artist.getDescription())
                .lpCount(lpCount)
                .build();
    }

    public void updateEntity(Artist existing, ArtistRequest request) {
        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
    }

}
