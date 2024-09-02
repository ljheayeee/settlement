package com.settlement.project.ads.dto;

import com.settlement.project.ads.entity.Ad;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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