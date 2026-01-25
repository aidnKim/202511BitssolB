package com.example.sideproject01.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.example.sideproject01.dto.SoundDto;
import com.example.sideproject01.entity.PlayHistory;
import com.example.sideproject01.entity.Sound;
import com.example.sideproject01.entity.User;
import com.example.sideproject01.repository.PlayHistoryRepository;
import com.example.sideproject01.repository.SoundRepository;
import com.example.sideproject01.repository.SoundTagRepository;
import com.example.sideproject01.repository.UserRepository;
import com.example.sideproject01.service.MyPageDeleteService;
import com.example.sideproject01.service.RecentPlayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class RestMyPageController {

    private final UserRepository userRepo;
    private final SoundRepository soundRepo;
    private final PlayHistoryRepository playHistoryRepo;
    private final RecentPlayService recentPlayService;
    private final MyPageDeleteService myPageDeleteService;
    private final SoundTagRepository soundTagRepo;
    

    private User getCurrentUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * ✅ 최근 재생 저장 (그대로 유지)
     */
    @PostMapping("/recent-plays/{soundId}")
    public void recordRecentPlay(@PathVariable Integer soundId) {
        recentPlayService.recordRecentPlay(soundId);
    }

    /**
     * ✅  최근 재생 "전체 리스트" 반환
     * - Oracle 11g에서 Pageable 쓰면 fetch first가 생성되어 ORA-00933이 나므로 제거
     * - 프론트에서 PAGE_SIZE로 잘라서 무한스크롤 처리
     */
    @GetMapping("/recent-plays")
    public List<SoundDto> recentPlays() {
        User user = getCurrentUser();

        List<PlayHistory> list = playHistoryRepo.findByUserIdOrderByPlayedAtDesc(user);

        return list.stream()
                .map(ph -> {
                    Sound s = ph.getSoundId();
                    List<String> tagNames = soundTagRepo.findBySoundId_SoundId(s.getSoundId())
                            .stream()
                            .map(st -> st.getTagId().getName())
                            .collect(Collectors.toList());
                    return SoundDto.toDto(s, s.getUploader(), tagNames); // ✅ 팀 규칙(기존 toDto 형태) 유지
                })
                .toList();
    }

    /**
     * ✅ 내 업로드 "전체 리스트" 반환
     * - 프론트에서 잘라서 무한스크롤 처리
     */
    @GetMapping("/uploads")
    public List<SoundDto> myUploads() {
        User user = getCurrentUser();

        List<Sound> list = soundRepo.findByUploader_IdOrderByCreatedAtDesc(user.getId());

        return list.stream()
        		.map(s -> {
        		    List<String> tagNames = soundTagRepo.findBySoundId_SoundId(s.getSoundId())
        		            .stream()
        		            .map(st -> st.getTagId().getName())
        		            .collect(Collectors.toList());
        		    return SoundDto.toDto(s, s.getUploader(), tagNames);
        		})
                .toList();
    }
    
    /**
     * ✅ 최근 재생 삭제
     * - "내 기록만" 지우는 개념(PlayHistory 1건 삭제)
     */
    
    @DeleteMapping("/recent-plays/{soundId}")
    public ResponseEntity<Void> deleteRecentPlay(@PathVariable Integer soundId) {
        myPageDeleteService.deleteMyRecentPlay(soundId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * ✅ 내 업로드 삭제(진짜 삭제)
     * - Sound + 연관(FK) 데이터 삭제 + 파일 삭제
     */

    @DeleteMapping("/uploads/{soundId}")
    public ResponseEntity<Void> deleteUpload(@PathVariable Integer soundId) {
        myPageDeleteService.deleteMyUpload(soundId);
        return ResponseEntity.ok().build();
    }
}