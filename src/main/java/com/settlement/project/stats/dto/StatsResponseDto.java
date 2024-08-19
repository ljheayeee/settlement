package com.settlement.project.stats.dto;


import com.settlement.project.stats.entity.Stats;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsResponseDto {

    private Long id;
    private Long videoId;
    private Long adId;
    private Integer statsAdView;

    public static StatsResponseDto fromEntity(Stats stats) {
        return StatsResponseDto.builder()
                .id(stats.getId())
                .videoId(stats.getVideoId())
                .adId(stats.getAdId())
                .statsAdView(stats.getStatsAdView())
                .build();
    }

}
