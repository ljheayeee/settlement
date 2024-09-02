package com.settlement.project.videoadstats.dto;

import com.settlement.project.ads.entity.Ad;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayAdResponseDto {
    private Long id;
    private String adUrl;
    private int adPlaytime;

    public static PlayAdResponseDto fromEntity(Ad ad) {
        return PlayAdResponseDto.builder()
                .id(ad.getId())
                .adUrl(ad.getAdUrl())
                .adPlaytime(ad.getAdPlaytime())
                .build();
    }
}
