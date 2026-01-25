package com.example.sideproject01.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.sideproject01.dto.ProgressResponseDto;
import com.example.sideproject01.entity.PlayProgress;
import com.example.sideproject01.entity.Sound;
import com.example.sideproject01.entity.User;
import com.example.sideproject01.repository.PlayProgressRepository;
import com.example.sideproject01.repository.SoundRepository;
import com.example.sideproject01.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {
    private final PlayProgressRepository progressRepo;
    private final SoundRepository soundRepo;
    private final UserRepository userRepo;
    private User getCurrentUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
    @Override
    @Transactional
    public void saveProgress(Integer soundId, Integer lastPosition, Integer duration) {
        User user = getCurrentUser();
        Sound sound = soundRepo.findById(soundId)
                .orElseThrow(() -> new IllegalArgumentException("사운드를 찾을 수 없습니다."));
        
        PlayProgress progress = progressRepo.findByUserAndSound(user, sound)
                .orElse(PlayProgress.builder()
                        .user(user)
                        .sound(sound)
                        .build());
        
        progress.setLastPosition(lastPosition);
        progress.setDuration(duration);
        progress.setUpdatedAt(LocalDateTime.now());
        
        progressRepo.save(progress);
    }
    @Override
    public List<ProgressResponseDto> getMyProgress() {
        User user = getCurrentUser();
        return progressRepo.findByUser(user).stream()
                .map(p -> ProgressResponseDto.builder()
                        .soundId(p.getSound().getSoundId())
                        .lastPosition(p.getLastPosition())
                        .duration(p.getDuration())
                        .build())
                .collect(Collectors.toList());
    }
}
