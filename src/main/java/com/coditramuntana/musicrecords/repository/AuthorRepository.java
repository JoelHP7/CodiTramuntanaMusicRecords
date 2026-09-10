package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findAllByOrderByNameAsc();

    /**
     * Resolves several authors by name in a single statement, avoiding one query per name.
     */
    @Query("select a from Author a where lower(a.name) in :names")
    List<Author> findByLowerNameIn(@Param("names") Collection<String> lowerCaseNames);

}
