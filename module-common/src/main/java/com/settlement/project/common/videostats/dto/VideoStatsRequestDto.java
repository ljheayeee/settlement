package com.settlement.project.common.videostats.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Builder
public class VideoStatsRequestDto {
    private String period;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private String sortBy; // "views" or "watchTime"

}
