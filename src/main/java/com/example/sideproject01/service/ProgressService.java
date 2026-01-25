package com.example.sideproject01.service;

import java.util.List;

import com.example.sideproject01.dto.ProgressResponseDto;

public interface ProgressService {
    void saveProgress(Integer soundId, Integer lastPosition, Integer duration);
    List<ProgressResponseDto> getMyProgress();
}
