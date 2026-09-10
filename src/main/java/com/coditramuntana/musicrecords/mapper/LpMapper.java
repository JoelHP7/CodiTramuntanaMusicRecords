package com.coditramuntana.musicrecords.mapper;

import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.data.Song;
import com.coditramuntana.musicrecords.model.dto.LpDetailDto;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class LpMapper {

    private final SongMapper songMapper;

    public Lp toEntity(LpRequest request, Artist artist) {
        return Lp.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .artist(artist)
                .build();
    }

    public LpDto toDto(Lp lp) {
        return LpDto.builder()
                .id(lp.getId())
                .name(lp.getName())
                .description(lp.getDescription())
                .artistId(lp.getArtist().getId())
                .artistName(lp.getArtist().getName())
                .build();
    }

    public LpDetailDto toDetailDto(Lp lp) {
        return LpDetailDto.builder()
                .id(lp.getId())
                .name(lp.getName())
                .description(lp.getDescription())
                .artistId(lp.getArtist().getId())
                .artistName(lp.getArtist().getName())
                .songs(lp.getSongs().stream()
                        .sorted(Comparator.comparing(Song::getName))
                        .map(songMapper::toDto)
                        .toList())
                .build();
    }

    public void updateEntity(Lp existing, LpRequest request, Artist artist) {
        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
        existing.setArtist(artist);
    }

}
