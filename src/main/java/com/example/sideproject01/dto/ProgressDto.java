package com.example.sideproject01.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDto {
    private Integer lastPosition;  // 초 단위
    private Integer duration;      // 전체 길이
}
