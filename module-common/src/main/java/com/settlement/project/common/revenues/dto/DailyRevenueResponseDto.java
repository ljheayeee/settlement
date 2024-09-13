package com.settlement.project.common.revenues.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyRevenueResponseDto {
    private final LocalDate date;
    private final Long totalRevenue;
    private final Long totalRevenueVideo;
    private final Long totalRevenueAd;

    public static DailyRevenueResponseDto from(LocalDate date, Long totalRevenueVideo, Long totalRevenueAd) {
        return builder()
                .date(date)
                .totalRevenueVideo(totalRevenueVideo)
                .totalRevenueAd(totalRevenueAd)
                .totalRevenue(totalRevenueVideo + totalRevenueAd)
                .build();
    }
}