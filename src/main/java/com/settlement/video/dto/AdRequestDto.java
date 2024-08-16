package com.settlement.video.dto;

import com.settlement.video.entity.Ad;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdRequestDto {

    private String adUrl;
    private LocalDate endDate;
    private boolean isUsed;

    public Ad toEntity() {
        return Ad.builder()
                .adUrl(adUrl)
                .endDate(endDate)
                .isUsed(isUsed)
                .build();
    }
}
