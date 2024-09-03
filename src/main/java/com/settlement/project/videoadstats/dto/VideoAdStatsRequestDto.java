package com.settlement.project.videoadstats.dto;

import com.settlement.project.videoadstats.entity.VideoAdStats;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VideoAdStatsRequestDto {

    private Long videoId;
    private Long adId;
    private Long dailyAdView;
    private Long totalAdView;

    public VideoAdStats toEntity(Long videoId, Long adId) {
        return VideoAdStats.builder()
                .videoId(videoId)
                .adId(adId)
                .dailyAdView(this.dailyAdView)
                .totalAdView(this.totalAdView)
                .build();
    }
}