package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.mapper.AuthorMapper;
import com.coditramuntana.musicrecords.model.data.Author;
import com.coditramuntana.musicrecords.repository.AuthorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Test
    @DisplayName("Should resolve every requested author with a single query")
    void resolveOrCreate_shouldQueryAllNamesInASingleCall() {
        when(authorRepository.findByLowerNameIn(anyCollection())).thenReturn(List.of(
                Author.builder().id(1L).name("Hetfield").build(),
                Author.builder().id(2L).name("Hammett").build()));

        authorService.resolveOrCreate(List.of("Hetfield", "Hammett"));

        verify(authorRepository, times(1)).findByLowerNameIn(anyCollection());
        verify(authorRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should reuse an existing author instead of creating a duplicate")
    void resolveOrCreate_shouldReuseExistingAuthorIgnoringCase() {
        Author hetfield = Author.builder().id(1L).name("Hetfield").build();
        when(authorRepository.findByLowerNameIn(anyCollection())).thenReturn(List.of(hetfield));

        Set<Author> resolved = authorService.resolveOrCreate(List.of("  hetfield "));

        assertThat(resolved).containsExactly(hetfield);
        verify(authorRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should create only the authors that do not exist yet")
    void resolveOrCreate_shouldCreateOnlyMissingAuthors() {
        Author hetfield = Author.builder().id(1L).name("Hetfield").build();
        when(authorRepository.findByLowerNameIn(anyCollection())).thenReturn(List.of(hetfield));
        when(authorRepository.saveAll(any()))
                .thenReturn(List.of(Author.builder().id(9L).name("Newton").build()));

        authorService.resolveOrCreate(List.of("Hetfield", "Newton"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Author>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(authorRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Author::getName).containsExactly("Newton");
    }

    @Test
    @DisplayName("Should collapse duplicated names ignoring case and surrounding whitespace")
    void resolveOrCreate_shouldDeduplicateInputIgnoringCaseAndWhitespace() {
        when(authorRepository.findByLowerNameIn(anyCollection())).thenReturn(List.of());
        when(authorRepository.saveAll(any()))
                .thenReturn(List.of(Author.builder().id(1L).name("Hetfield").build()));

        authorService.resolveOrCreate(List.of("Hetfield", " hetfield ", "HETFIELD"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Author>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(authorRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("Should return an empty set when no usable name is provided")
    void resolveOrCreate_shouldReturnEmptySetWhenNamesAreBlank() {
        Set<Author> resolved = authorService.resolveOrCreate(List.of("   ", ""));

        assertThat(resolved).isEmpty();
        verify(authorRepository, never()).findByLowerNameIn(anyCollection());
    }

}
