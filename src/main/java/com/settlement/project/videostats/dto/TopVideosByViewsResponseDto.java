package com.settlement.project.videostats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopVideosByViewsResponseDto {
    private Long videoId;
    private Long totalViews;

    public static TopVideosByViewsResponseDto fromEntity(Long videoId, Long totalViews) {
        return TopVideosByViewsResponseDto.builder()
                .videoId(videoId)
                .totalViews(totalViews)
                .build();
    }
}
