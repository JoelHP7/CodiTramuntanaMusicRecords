package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    boolean existsByLpIdAndNameIgnoreCase(Long lpId, String name);

    boolean existsByLpIdAndNameIgnoreCaseAndIdNot(Long lpId, String name, Long id);

    @Query("select s from Song s left join fetch s.authors where s.id = :id")
    Optional<Song> findByIdWithAuthors(@Param("id") Long id);

}
