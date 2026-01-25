package com.example.sideproject01.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponseDto {
    private Integer soundId;
    private Integer lastPosition;
    private Integer duration;
}
