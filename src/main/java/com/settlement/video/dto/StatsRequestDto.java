package com.settlement.video.dto;

import com.settlement.video.entity.Ad;
import com.settlement.video.entity.Stats;
import com.settlement.video.entity.Video;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class StatsRequestDto {

    private Long videoId;
    private Long adId;
    private Integer statsAdView;

    public Stats toEntity(Video video, Ad ad) {
        return Stats.builder()
                .video(video)
                .ad(ad)
                .statsAdView(statsAdView)
                .build();
    }
}
