package com.settlement.project.common.videostats.dto;

import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.common.videostats.repository.VideoStatsRepository;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoStatsResponseDto {
    private final VideoStatsRepository videoStatsRepository;
    private final VideoRepository videoRepository;

    private Long videoId;
    private long views;  // int에서 long으로 변경
    private long watchTime;
    private String period;

    public static VideoStatsResponseDto fromEntity(Long videoId, long views, long watchTime, String period) {
        return builder()
                .videoId(videoId)
                .views(views)
                .watchTime(watchTime)
                .period(period)
                .build();
    }

}