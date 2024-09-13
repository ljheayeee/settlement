package com.settlement.project.main.videoadstats.dto;

import com.settlement.project.common.ads.entity.Ad;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayAdResponseDto {
    private Long id;
    private String adUrl;
    private int adPlaytime;

    public static PlayAdResponseDto fromEntity(Ad ad) {
        return builder()
                .id(ad.getId())
                .adUrl(ad.getAdUrl())
                .adPlaytime(ad.getAdPlaytime())
                .build();
    }
}
