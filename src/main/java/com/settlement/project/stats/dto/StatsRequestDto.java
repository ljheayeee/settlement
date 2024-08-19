package com.settlement.project.stats.dto;

import com.settlement.project.ads.entity.Ad;
import com.settlement.project.stats.entity.Stats;
import com.settlement.project.video.entity.Video;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class StatsRequestDto {

    private Long videoId;
    private Long adId;
    private Integer statsAdView;

    public Stats toEntity(Long videoId,Long adId,Integer statsAdView) {
        return Stats.builder()
                .videoId(videoId)
                .adId(adId)
                .statsAdView(statsAdView)
                .build();
    }
}
