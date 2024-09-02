package com.settlement.project.videoadstats.dto;


import com.settlement.project.videoadstats.entity.VideoAdStats;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoAdStatsResponseDto {

    private Long id;
    private Long videoId;
    private Long adId;
    private Long statsAdView;

    public static VideoAdStatsResponseDto fromEntity(VideoAdStats videoAdStats) {
        return VideoAdStatsResponseDto.builder()
                .id(videoAdStats.getId())
                .videoId(videoAdStats.getVideoId())
                .adId(videoAdStats.getAdId())
                .statsAdView(videoAdStats.getStatsAdView())
                .build();
    }

}
