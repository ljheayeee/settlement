package com.settlement.project.common.videostats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopVideosByViewsResponseDto {
    private Long videoId;
    private Long totalViews;

    public static TopVideosByViewsResponseDto fromEntity(Long videoId, Long totalViews) {
        return builder()
                .videoId(videoId)
                .totalViews(totalViews)
                .build();
    }
}
