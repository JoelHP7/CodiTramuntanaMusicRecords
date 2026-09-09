package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.exception.ArtistAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.ArtistHasLpsException;
import com.coditramuntana.musicrecords.exception.ArtistNotFoundException;
import com.coditramuntana.musicrecords.mapper.ArtistMapper;
import com.coditramuntana.musicrecords.mapper.LpMapper;
import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.dto.ArtistDetailDto;
import com.coditramuntana.musicrecords.model.dto.ArtistDto;
import com.coditramuntana.musicrecords.model.dto.ArtistRequest;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final LpRepository lpRepository;
    private final ArtistMapper artistMapper;
    private final LpMapper lpMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ArtistDto> getArtists(String name) {
        List<Artist> artists = StringUtils.hasText(name)
                ? artistRepository.findByNameContainingIgnoreCaseOrderByName(name.trim())
                : artistRepository.findAllByOrderByNameAsc();
        log.info("Found {} artists (filter='{}')", artists.size(), name);
        return artists.stream().map(artistMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistDetailDto getArtist(Long id) {
        Artist artist = findArtistOrThrow(id);
        // A COUNT statement, instead of loading the whole collection just to read its size.
        long lpCount = lpRepository.countByArtistId(id);
        log.info("Artist id={} has {} LP(s)", id, lpCount);
        return artistMapper.toDetailDto(artist, lpCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LpDto> getArtistLps(Long id) {
        ensureArtistExists(id);
        return lpRepository.findByArtistId(id).stream().map(lpMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ArtistDto createArtist(ArtistRequest request) {
        String name = request.getName().trim();
        log.info("Creating artist: name='{}'", name);

        validateNameIsFree(name);

        Artist saved = artistRepository.save(artistMapper.toEntity(request));
        log.info("Artist created: id={}, name='{}'", saved.getId(), saved.getName());
        return artistMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ArtistDto updateArtist(Long id, ArtistRequest request) {
        String name = request.getName().trim();
        log.info("Updating artist id={} with name='{}'", id, name);

        Artist existing = findArtistOrThrow(id);
        validateNameIsFreeForOther(name, id);

        artistMapper.updateEntity(existing, request);
        Artist updated = artistRepository.save(existing);
        log.info("Artist id={} updated", updated.getId());
        return artistMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteArtist(Long id) {
        log.info("Deleting artist id={}", id);
        Artist existing = findArtistOrThrow(id);

        long lpCount = lpRepository.countByArtistId(id);
        if (lpCount > 0) {
            log.warn("Artist id={} still owns {} LP(s), deletion rejected", id, lpCount);
            throw new ArtistHasLpsException(existing.getName(), lpCount);
        }

        artistRepository.delete(existing);
        log.info("Artist id={} deleted", id);
    }

    private Artist findArtistOrThrow(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Artist not found: id={}", id);
                    return new ArtistNotFoundException(id);
                });
    }

    private void ensureArtistExists(Long id) {
        if (!artistRepository.existsById(id)) {
            log.warn("Artist not found: id={}", id);
            throw new ArtistNotFoundException(id);
        }
    }

    /**
     * SQLite unique indexes are case sensitive, so uniqueness is also enforced here.
     */
    private void validateNameIsFree(String name) {
        if (artistRepository.existsByNameIgnoreCase(name)) {
            log.warn("Duplicated artist name: '{}'", name);
            throw new ArtistAlreadyExistsException(name);
        }
    }

    private void validateNameIsFreeForOther(String name, Long id) {
        if (artistRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            log.warn("Duplicated artist name: '{}'", name);
            throw new ArtistAlreadyExistsException(name);
        }
    }

}
