package com.example.sideproject01.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sideproject01.dto.SoundDto;
import com.example.sideproject01.dto.SoundUploadRequestDto;
import com.example.sideproject01.entity.Sound;
import com.example.sideproject01.entity.SoundTag;
import com.example.sideproject01.entity.Tag;
import com.example.sideproject01.entity.User;
import com.example.sideproject01.repository.SoundRepository;
import com.example.sideproject01.repository.SoundTagRepository;
import com.example.sideproject01.repository.TagRepository;
import com.example.sideproject01.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoundServiceImpl implements SoundService {

	private final SoundRepository soundRepo;
	private final UserRepository userRepo;
	private final TagRepository tagRepo;
	private final SoundTagRepository soundTagRepo;

	@Value("${file.location}")
	private String fileLocation; // 파일을 저장할 위치

	@Override
	@Transactional(readOnly = true)
	public List<SoundDto> getAll(String sortBy, String keyword, List<Integer> tagIds) {
		List<Sound> sounds;

		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		boolean hasTags = tagIds != null && !tagIds.isEmpty();
		boolean isPopularity = "popularity".equals(sortBy);

		if (hasKeyword && hasTags) {
			// 키워드 + 태그 필터
			sounds = isPopularity
					? soundRepo.findByKeywordAndTagIdsPopularity(keyword, tagIds)
					: soundRepo.findByKeywordAndTagIdsLatest(keyword, tagIds);
		} else if (hasKeyword) {
			// 키워드만
			sounds = isPopularity
					? soundRepo.findByKeywordPopularity(keyword)
					: soundRepo.findByKeywordLatest(keyword);
		} else if (hasTags) {
			// 태그만
			sounds = isPopularity
					? soundRepo.findByTagIdsPopularity(tagIds)
					: soundRepo.findByTagIdsLatest(tagIds);
		} else {
			// 필터 없음
			sounds = isPopularity
					? soundRepo.findAllWithUploaderByPopularity()
					: soundRepo.findAllWithUploader();
		}

		return sounds.stream()
				.map(sound ->{
					// 각 Sound의 태그 조회
		            List<String> tagNames = soundTagRepo.findBySoundId_SoundId(sound.getSoundId())
		                    .stream()
		                    .map(st -> st.getTagId().getName())
		                    .collect(Collectors.toList());
		            return SoundDto.toDto(sound, sound.getUploader(), tagNames);
				})
				.toList();
	}

	@Override
	public SoundDto saveSound(SoundUploadRequestDto requestDto, MultipartFile soundFile,
			MultipartFile thumbnailFile) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();

		User uploader = userRepo.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));

		String title = requestDto.getTitle();
		MultipartFile sound = soundFile;
		MultipartFile thumbnail = thumbnailFile;

		// 원본 파일명
		String orgFileName = sound.getOriginalFilename();
		// 파일의 크기
		long fileSize = sound.getSize();
		// 저장할 파일의 이름을 Universal Unique 한 문자열로 얻어내기
		String saveFileName = UUID.randomUUID().toString() + orgFileName;
		// 저장할 파일의 전체 경로 구성하기
		String filePath = fileLocation + File.separator + saveFileName;
		try {
			// 업로드할 파일을 저장할 파일 객체 생성
			File saveFile = new File(filePath);
			// 원하는 곳으로 파일을 이동시킨다(원하는 곳에 파일을 저장한다)
			sound.transferTo(saveFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 파일명만 저장 (URL은 SoundDto.toDto()에서 생성)

		// 원본 파일명
		String orgFileName2 = thumbnail.getOriginalFilename();
		// 파일의 크기
		long fileSize2 = thumbnail.getSize();
		// 저장할 파일의 이름을 Universal Unique 한 문자열로 얻어내기
		String saveFileName2 = UUID.randomUUID().toString() + orgFileName2;
		// 저장할 파일의 전체 경로 구성하기
		String filePath2 = fileLocation + File.separator + saveFileName2;
		try {
			// 업로드할 파일을 저장할 파일 객체 생성
			File saveFile2 = new File(filePath2);
			// 원하는 곳으로 파일을 이동시킨다(원하는 곳에 파일을 저장한다)
			thumbnail.transferTo(saveFile2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String thumbnailUrl = "/upload/" + saveFileName2;

		Sound soundEntity = Sound.builder()
				.title(title)
				.description(requestDto.getDescription())
				.uploader(uploader)
				.fileUrl(saveFileName)
				.thumbnailUrl(thumbnailUrl)
				.createdAt(LocalDateTime.now())
				.build();

		soundRepo.save(soundEntity);

		// 태그 저장
		if (requestDto.getTagIds() != null && !requestDto.getTagIds().isEmpty()) {
			for (Integer tagId : requestDto.getTagIds()) {
				Tag tag = tagRepo.findById(tagId)
						.orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다: " + tagId));

				SoundTag soundTag = SoundTag.builder()
						.soundId(soundEntity)
						.tagId(tag)
						.build();

				soundTagRepo.save(soundTag);
			}
		}

		return null;
	}

	@Override
	public SoundDto getSoundById(Integer soundId) {
		Sound sound = soundRepo.findById(soundId).orElseThrow(() -> new IllegalArgumentException("해당 소리를 찾을 수 없습니다."));
	    User uploader = sound.getUploader();
	    
	    // 태그 조회
	    List<String> tagNames = soundTagRepo.findBySoundId_SoundId(soundId)
	            .stream()
	            .map(st -> st.getTagId().getName())
	            .collect(Collectors.toList());
	    
	    return SoundDto.toDto(sound, uploader, tagNames);
	}

	@Override
	public String getSoundFileUrl(Integer soundId) {
		Sound sound = soundRepo.findById(soundId)
				.orElseThrow(() -> new IllegalArgumentException("해당 소리 파일을 찾을 수 없습니다."));

		String fileUrl = sound.getFileUrl();
		// 만약 전체 URL이 저장되어 있다면 파일명만 추출
		if (fileUrl.contains("/")) {
			return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		}
		return fileUrl;
	}

	@Override
	@Transactional
	public void incrementPlayCount(Integer soundId) {
		soundRepo.incrementPlayCount(soundId);
	}

}
