package com.settlement.project.ads.dto;

import com.settlement.project.ads.entity.Ad;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdResponseDto {
    private Long id;
    private String adUrl;
    private LocalDate endDate;
    private boolean isUsed;

    public static AdResponseDto fromEntity(Ad ad) {
        return AdResponseDto.builder()
                .id(ad.getId())
                .adUrl(ad.getAdUrl())
                .endDate(ad.getEndDate())
                .isUsed(ad.isUsed())
                .build();
    }
}
