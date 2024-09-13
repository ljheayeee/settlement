package com.settlement.project.common.ads.dto;

import com.settlement.project.common.ads.entity.Ad;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdRequestDto {
    private String adUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int adPlaytime;
    private boolean isUsed;

    public Ad toEntity() {
        return Ad.builder()
                .adUrl(adUrl)
                .startDate(startDate)
                .endDate(endDate)
                .adPlaytime(adPlaytime)
                .isUsed(isUsed)
                .build();
    }
}