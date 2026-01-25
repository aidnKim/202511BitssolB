package com.example.sideproject01.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.sideproject01.dto.ProgressDto;
import com.example.sideproject01.dto.ProgressResponseDto;
import com.example.sideproject01.dto.SoundDto;
import com.example.sideproject01.dto.SoundUploadRequestDto;
import com.example.sideproject01.service.ProgressService;
import com.example.sideproject01.service.SoundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class RestSoundController {

	private final SoundService soundService;
	private final ProgressService progressService;

    @Value("${file.location}")
    private String fileLocation; // 파일을 저장할 위치

    // 소리 파일 목록 조회
    @GetMapping("/sounds")
    public List<SoundDto> list(
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tagIds) {
        List<Integer> tagIdList = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            tagIdList = java.util.Arrays.stream(tagIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
        }
        List<SoundDto> list = soundService.getAll(sortBy, keyword, tagIdList);
        return list;
    }

    // 소리 파일 저장
    @PostMapping(value = "/sounds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SoundDto> uploadSound(
            @Valid @RequestPart("dto") SoundUploadRequestDto requestDto,
            @RequestPart("soundFile") MultipartFile soundFile,
            @RequestPart("thumbnailFile") MultipartFile thumbnailFile) {
        SoundDto dto = soundService.saveSound(requestDto, soundFile, thumbnailFile);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SoundDto());

    }

    // 특정 소리 파일 가져오기
    @GetMapping("/sounds/{soundId}")
    public ResponseEntity<SoundDto> getSound(@PathVariable Integer soundId) {
        SoundDto dto = soundService.getSoundById(soundId);
        return ResponseEntity.ok(dto);
    }

    // 소리 파일 stream
    @GetMapping("/sounds/stream/{soundId}")
    public ResponseEntity<ResourceRegion> streamSound(
            @PathVariable Integer soundId,
            @RequestHeader HttpHeaders headers) throws IOException {

        String fileUrl = soundService.getSoundFileUrl(soundId);
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        Path filePath = Paths.get(fileLocation + File.separator + filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        long contentLength = resource.contentLength();

        // Range 헤더 파싱
        HttpRange range = headers.getRange().isEmpty()
                ? null
                : headers.getRange().get(0);

        ResourceRegion region;
        HttpStatus status; // 상태 코드 변수 추가

        if (range != null) {
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1 * 1024 * 1024, end - start + 1);
            region = new ResourceRegion(resource, start, rangeLength);
            status = HttpStatus.PARTIAL_CONTENT; // Range 요청이 있으면 206
        } else {
            region = new ResourceRegion(resource, 0, contentLength);
            status = HttpStatus.OK; // Range 요청이 없으면 200
        }

        // Content-Type 동적 결정
        String contentType = filename.endsWith(".mp3") ? "audio/mpeg"
                : filename.endsWith(".wav") ? "audio/wav"
                        : "audio/mpeg"; // 기본값

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
    }

    // 조회수 증가
    @PostMapping("/sounds/{soundId}/play")
    public ResponseEntity<Void> incrementPlayCount(@PathVariable Integer soundId) {
        soundService.incrementPlayCount(soundId);
        return ResponseEntity.ok().build();
    }
    
    //재생 위치 저장
    @PutMapping("/sounds/{soundId}/progress")
    public ResponseEntity<?> saveProgress(
        @PathVariable Integer soundId,
        @RequestBody ProgressDto dto  // { lastPosition: 120, duration: 300 }
    ) {
        progressService.saveProgress(soundId, dto.getLastPosition(), dto.getDuration());
        return ResponseEntity.ok().build();
    }
    
    //내 진행률 조회
    @GetMapping("/sounds/progress")
    public List<ProgressResponseDto> getMyProgress() {
        return progressService.getMyProgress();
    }


}
