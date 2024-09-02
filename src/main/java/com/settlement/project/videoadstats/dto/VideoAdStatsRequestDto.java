package com.settlement.project.videoadstats.dto;

import com.settlement.project.videoadstats.entity.VideoAdStats;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class VideoAdStatsRequestDto {

    private Long videoId;
    private Long adId;
    private Long statsAdView;

    public VideoAdStats toEntity(Long videoId, Long adId, Long statsAdView) {
        return VideoAdStats.builder()
                .videoId(videoId)
                .adId(adId)
                .statsAdView(statsAdView)
                .build();
    }
}
