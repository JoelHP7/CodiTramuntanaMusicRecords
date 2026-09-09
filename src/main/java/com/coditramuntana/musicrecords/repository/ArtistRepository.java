package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    /**
     * SQLite unique indexes are case sensitive, so uniqueness is enforced here as well.
     */
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Artist> findByNameIgnoreCase(String name);

    List<Artist> findAllByOrderByNameAsc();

    @Query("select a from Artist a where lower(a.name) like lower(concat('%', :name, '%')) order by a.name asc")
    List<Artist> findByNameContainingIgnoreCaseOrderByName(@Param("name") String name);

}
