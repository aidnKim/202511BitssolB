package com.example.sideproject01.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.sideproject01.entity.Sound;
import com.example.sideproject01.entity.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SoundDto {
	private Integer soundId;
	private String uploader;

	@NotBlank(message = "제목은 필수 입니다")
	private String title;

	@NotBlank(message = "설명은 필수 입니다")
	private String description;

	private String fileUrl;
	private String thumbnailUrl;
	private Integer playCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<String> tags;

	 public static SoundDto toDto(Sound entity, User userEntity, List<String> tagNames) {
		return SoundDto.builder()
				.soundId(entity.getSoundId())
				.uploader(userEntity.getUserName())
				.title(entity.getTitle())
				.description(entity.getDescription())
				.fileUrl("/v1/sounds/stream/" + entity.getSoundId())
				.thumbnailUrl(entity.getThumbnailUrl())
				.playCount(entity.getPlayCount())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.tags(tagNames)
				.build();
	}

	public Sound toEntity(User userEntity) {
		return Sound.builder()
				.soundId(this.soundId)
				.uploader(userEntity)
				.title(this.title)
				.description(this.description)
				.fileUrl(this.fileUrl)
				.thumbnailUrl(this.thumbnailUrl)
				.playCount(this.playCount)
				.createdAt(this.createdAt)
				.updatedAt(this.updatedAt)
				.build();
	}
}
