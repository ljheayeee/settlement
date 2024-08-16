package com.settlement.video.dto;


import com.settlement.video.entity.Stats;
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
                .videoId(stats.getVideo().getId())
                .adId(stats.getAd().getId())
                .statsAdView(stats.getStatsAdView())
                .build();
    }

}
