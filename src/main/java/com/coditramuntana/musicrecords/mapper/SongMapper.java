package com.coditramuntana.musicrecords.mapper;

import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.data.Song;
import com.coditramuntana.musicrecords.model.dto.SongDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class SongMapper {

    public SongDto toDto(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .name(song.getName())
                .authors(song.getAuthors().stream()
                        .map(Author::getName)
                        .sorted(Comparator.naturalOrder())
                        .toList())
                .build();
    }

}
