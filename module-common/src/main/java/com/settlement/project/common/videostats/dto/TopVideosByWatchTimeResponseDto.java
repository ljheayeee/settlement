package com.settlement.project.common.videostats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopVideosByWatchTimeResponseDto {
    private Long videoId;
    private Long totalWatchTime;

    public static TopVideosByWatchTimeResponseDto fromEntity(Long videoId, Long totalWatchTime) {
        return builder()
                .videoId(videoId)
                .totalWatchTime(totalWatchTime)
                .build();
    }
}
