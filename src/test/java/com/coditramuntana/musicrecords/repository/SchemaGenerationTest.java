package com.coditramuntana.musicrecords.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that Hibernate really generates the expected schema on SQLite.
 *
 * <p>{@code replace = NONE} is mandatory: otherwise Spring would swap the SQLite
 * datasource for an embedded one, which is exactly what these tests must prevent.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaGenerationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Hibernate should create every table of the domain model on SQLite")
    void schema_shouldContainAllDomainTables() {
        @SuppressWarnings("unchecked")
        List<String> tables = entityManager
                .createNativeQuery("select name from sqlite_master where type = 'table'")
                .getResultList();

        assertThat(tables).contains("artists", "lps", "songs", "authors", "song_authors");
    }

    @Test
    @DisplayName("LP identifiers should be integer primary keys so SQLite autoincrement works")
    void schema_shouldDeclareIntegerPrimaryKeyForLps() {
        String ddl = (String) entityManager
                .createNativeQuery("select sql from sqlite_master where type = 'table' and name = 'lps'")
                .getSingleResult();

        assertThat(ddl.toLowerCase()).contains("id integer");
        assertThat(ddl.toLowerCase()).contains("primary key");
    }

    @Test
    @DisplayName("The join table of the many-to-many association should exist")
    void schema_shouldCreateSongAuthorsJoinTable() {
        String ddl = (String) entityManager
                .createNativeQuery("select sql from sqlite_master where type = 'table' and name = 'song_authors'")
                .getSingleResult();

        assertThat(ddl.toLowerCase()).contains("song_id");
        assertThat(ddl.toLowerCase()).contains("author_id");
    }

}
