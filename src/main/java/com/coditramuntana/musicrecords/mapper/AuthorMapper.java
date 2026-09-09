package com.coditramuntana.musicrecords.mapper;

import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.dto.AuthorDto;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public AuthorDto toDto(Author author) {
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }

}
