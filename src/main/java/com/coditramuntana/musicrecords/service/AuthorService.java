package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.dto.AuthorDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AuthorService {

    List<AuthorDto> getAllAuthors();

    /**
     * Returns the authors matching the given names, creating only the missing ones.
     */
    Set<Author> resolveOrCreate(Collection<String> names);

}
