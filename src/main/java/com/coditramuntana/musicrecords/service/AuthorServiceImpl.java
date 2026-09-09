package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.mapper.AuthorMapper;
import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.model.dto.AuthorDto;
import com.coditramuntana.musicrecords.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages the shared catalogue of song authors.
 *
 * <p>Authors are never deleted automatically: they are a reusable catalogue, so removing
 * a song must not remove the authors it referenced.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        List<Author> authors = authorRepository.findAllByOrderByNameAsc();
        log.info("Found {} authors in the catalogue", authors.size());
        return authors.stream().map(authorMapper::toDto).toList();
    }

    @Override
    @Transactional
    public Set<Author> resolveOrCreate(Collection<String> names) {
        // Normalise and de-duplicate ignoring case, keeping the order the caller asked for.
        Map<String, String> normalisedByKey = names.stream()
                .map(AuthorServiceImpl::normalise)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toMap(
                        AuthorServiceImpl::key,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new));

        if (normalisedByKey.isEmpty()) {
            return new LinkedHashSet<>();
        }

        // A single query resolves every requested name, instead of one query per name.
        Map<String, Author> existingByKey = authorRepository.findByLowerNameIn(normalisedByKey.keySet()).stream()
                .collect(Collectors.toMap(author -> key(author.getName()), Function.identity()));

        Set<Author> resolved = new LinkedHashSet<>();
        List<Author> toCreate = normalisedByKey.entrySet().stream()
                .filter(entry -> !existingByKey.containsKey(entry.getKey()))
                .map(entry -> Author.builder().name(entry.getValue()).build())
                .toList();

        if (!toCreate.isEmpty()) {
            authorRepository.saveAll(toCreate).forEach(author -> existingByKey.put(key(author.getName()), author));
            log.info("Created {} new author(s): {}", toCreate.size(), toCreate.stream().map(Author::getName).toList());
        }

        normalisedByKey.keySet().forEach(name -> resolved.add(existingByKey.get(name)));
        return resolved;
    }

    private static String normalise(String name) {
        return name == null ? "" : name.trim().replaceAll("\s+", " ");
    }

    private static String key(String name) {
        return normalise(name).toLowerCase(Locale.ROOT);
    }

}
