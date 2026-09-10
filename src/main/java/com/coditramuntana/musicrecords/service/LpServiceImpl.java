package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.exception.ArtistNotFoundException;
import com.coditramuntana.musicrecords.exception.LpAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.LpNotFoundException;
import com.coditramuntana.musicrecords.exception.SongAlreadyExistsException;
import com.coditramuntana.musicrecords.exception.SongNotFoundException;
import com.coditramuntana.musicrecords.mapper.LpMapper;
import com.coditramuntana.musicrecords.mapper.SongMapper;
import com.coditramuntana.musicrecords.model.data.Artist;
import com.coditramuntana.musicrecords.model.data.Lp;
import com.coditramuntana.musicrecords.model.data.Song;
import com.coditramuntana.musicrecords.model.dto.LpDetailDto;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import com.coditramuntana.musicrecords.model.dto.SongDto;
import com.coditramuntana.musicrecords.model.dto.SongRequest;
import com.coditramuntana.musicrecords.repository.ArtistRepository;
import com.coditramuntana.musicrecords.repository.LpRepository;
import com.coditramuntana.musicrecords.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Manages LPs and the songs they contain.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LpServiceImpl implements LpService {

    private final LpRepository lpRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final AuthorService authorService;
    private final LpMapper lpMapper;
    private final SongMapper songMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LpDto> getLps(String artistName) {
        // Two dedicated queries instead of a ":param is null" trick: the unfiltered one
        // does not need the LIKE, and both are trivially testable.
        List<Lp> lps = StringUtils.hasText(artistName)
                ? lpRepository.findByArtistNameContaining(artistName.trim())
                : lpRepository.findAllWithArtist();
        log.info("Found {} LPs (artist filter='{}')", lps.size(), artistName);
        return lps.stream().map(lpMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LpDetailDto getLp(Long id) {
        Lp lp = lpRepository.findDetailById(id)
                .orElseThrow(() -> {
                    log.warn("LP not found: id={}", id);
                    return new LpNotFoundException(id);
                });
        return lpMapper.toDetailDto(lp);
    }

    @Override
    @Transactional
    public LpDto createLp(LpRequest request) {
        String name = request.getName().trim();
        log.info("Creating LP: name='{}', artistId={}", name, request.getArtistId());

        Artist artist = findArtistOrThrow(request.getArtistId());
        validateLpNameIsFreeForArtist(artist, name);

        Lp saved = lpRepository.save(lpMapper.toEntity(request, artist));
        log.info("LP created: id={}, name='{}'", saved.getId(), saved.getName());
        return lpMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LpDto updateLp(Long id, LpRequest request) {
        String name = request.getName().trim();
        log.info("Updating LP id={} with name='{}', artistId={}", id, name, request.getArtistId());

        Lp existing = findLpOrThrow(id);
        Artist artist = findArtistOrThrow(request.getArtistId());
        validateLpNameIsFreeForOther(artist, name, id);

        lpMapper.updateEntity(existing, request, artist);
        Lp updated = lpRepository.save(existing);
        log.info("LP id={} updated", updated.getId());
        return lpMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteLp(Long id) {
        log.info("Deleting LP id={}", id);
        Lp existing = findLpOrThrow(id);
        // Songs are part of the LP aggregate and are removed with it.
        // Authors are a shared catalogue and survive.
        lpRepository.delete(existing);
        log.info("LP id={} deleted along with its songs", id);
    }

    @Override
    @Transactional
    public SongDto addSong(Long lpId, SongRequest request) {
        String name = request.getName().trim();
        log.info("Adding song '{}' to LP id={}", name, lpId);

        Lp lp = findLpOrThrow(lpId);
        if (songRepository.existsByLpIdAndNameIgnoreCase(lpId, name)) {
            log.warn("Duplicated song '{}' in LP id={}", name, lpId);
            throw new SongAlreadyExistsException(lp.getName(), name);
        }

        Song song = Song.builder().name(name).build();
        song.replaceAuthors(authorService.resolveOrCreate(request.getAuthorNames()));
        lp.addSong(song);

        // Persisted through the song repository rather than by cascading from the LP:
        // saving an already managed LP goes through merge, which would leave the
        // generated identifier out of the instance returned in the response.
        Song saved = songRepository.save(song);
        log.info("Song '{}' added to LP id={} with id={}", name, lpId, saved.getId());
        return songMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SongDto updateSong(Long lpId, Long songId, SongRequest request) {
        String name = request.getName().trim();
        log.info("Updating song id={} of LP id={}", songId, lpId);

        Lp lp = findLpOrThrow(lpId);
        Song song = findSongOfLpOrThrow(lpId, songId);

        if (songRepository.existsByLpIdAndNameIgnoreCaseAndIdNot(lpId, name, songId)) {
            log.warn("Duplicated song '{}' in LP id={}", name, lpId);
            throw new SongAlreadyExistsException(lp.getName(), name);
        }

        song.setName(name);
        song.replaceAuthors(authorService.resolveOrCreate(request.getAuthorNames()));

        songRepository.save(song);
        log.info("Song id={} updated", songId);
        return songMapper.toDto(song);
    }

    @Override
    @Transactional
    public void deleteSong(Long lpId, Long songId) {
        log.info("Deleting song id={} from LP id={}", songId, lpId);

        Lp lp = findLpOrThrow(lpId);
        Song song = findSongOfLpOrThrow(lpId, songId);

        // Orphan removal deletes the song; its authors stay in the catalogue.
        lp.removeSong(song);
        lpRepository.save(lp);
        log.info("Song id={} deleted from LP id={}", songId, lpId);
    }

    private Lp findLpOrThrow(Long id) {
        return lpRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("LP not found: id={}", id);
                    return new LpNotFoundException(id);
                });
    }

    private Artist findArtistOrThrow(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Artist not found: id={}", id);
                    return new ArtistNotFoundException(id);
                });
    }

    /**
     * Validates that the song exists and belongs to the given LP, so a song of another
     * LP cannot be modified through a wrong nested path.
     */
    private Song findSongOfLpOrThrow(Long lpId, Long songId) {
        return songRepository.findByIdWithAuthors(songId)
                .filter(song -> song.getLp() != null && song.getLp().getId().equals(lpId))
                .orElseThrow(() -> {
                    log.warn("Song id={} not found in LP id={}", songId, lpId);
                    return new SongNotFoundException(songId, lpId);
                });
    }

    private void validateLpNameIsFreeForArtist(Artist artist, String name) {
        if (lpRepository.existsByArtistIdAndNameIgnoreCase(artist.getId(), name)) {
            log.warn("Artist '{}' already has an LP named '{}'", artist.getName(), name);
            throw new LpAlreadyExistsException(artist.getName(), name);
        }
    }

    private void validateLpNameIsFreeForOther(Artist artist, String name, Long lpId) {
        if (lpRepository.existsByArtistIdAndNameIgnoreCaseAndIdNot(artist.getId(), name, lpId)) {
            log.warn("Artist '{}' already has an LP named '{}'", artist.getName(), name);
            throw new LpAlreadyExistsException(artist.getName(), name);
        }
    }

}
