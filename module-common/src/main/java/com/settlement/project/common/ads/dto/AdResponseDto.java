package com.settlement.project.common.ads.dto;

import com.settlement.project.common.ads.entity.Ad;
import com.settlement.project.common.ads.entity.AdStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdResponseDto {
    private Long id;
    private String adUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int adPlaytime;
    private AdStatusEnum status;
    private boolean isUsed;

    public static AdResponseDto fromEntity(Ad ad) {
        return builder()
                .id(ad.getId())
                .adUrl(ad.getAdUrl())
                .startDate(ad.getStartDate())
                .endDate(ad.getEndDate())
                .adPlaytime(ad.getAdPlaytime())
                .status(ad.getStatus())
                .isUsed(ad.isUsed())
                .build();
    }
}
