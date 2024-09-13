package com.settlement.project.main.videoadstats.dto;

import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoAdStatsResponseDto {

    private Long id;
    private Long videoId;
    private Long adId;
    private Long dailyAdView;
    private Long totalAdView;

    public static VideoAdStatsResponseDto fromEntity(VideoAdStats videoAdStats) {
        return builder()
                .id(videoAdStats.getId())
                .videoId(videoAdStats.getVideoId())
                .adId(videoAdStats.getAdId())
                .dailyAdView(videoAdStats.getDailyAdView())
                .totalAdView(videoAdStats.getTotalAdView())
                .build();
    }
}