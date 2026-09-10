package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.model.dto.LpDetailDto;
import com.coditramuntana.musicrecords.model.dto.LpDto;
import com.coditramuntana.musicrecords.model.dto.LpRequest;
import com.coditramuntana.musicrecords.model.dto.SongDto;
import com.coditramuntana.musicrecords.model.dto.SongRequest;

import java.util.List;

public interface LpService {

    List<LpDto> getLps(String artistName);

    LpDetailDto getLp(Long id);

    LpDto createLp(LpRequest request);

    LpDto updateLp(Long id, LpRequest request);

    void deleteLp(Long id);

    SongDto addSong(Long lpId, SongRequest request);

    SongDto updateSong(Long lpId, Long songId, SongRequest request);

    void deleteSong(Long lpId, Long songId);

}
