package com.settlement.project.videostats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopVideosByWatchTimeResponseDto {
    private Long videoId;
    private Long totalWatchTime;

    public static TopVideosByWatchTimeResponseDto fromEntity(Long videoId, Long totalWatchTime) {
        return TopVideosByWatchTimeResponseDto.builder()
                .videoId(videoId)
                .totalWatchTime(totalWatchTime)
                .build();
    }
}
