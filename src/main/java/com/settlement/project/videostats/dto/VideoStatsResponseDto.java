package com.settlement.project.videostats.dto;

import com.settlement.project.common.util.DateRange;
import com.settlement.project.video.entity.Video;
import com.settlement.project.video.repository.VideoRepository;
import com.settlement.project.videostats.entity.VideoStats;
import com.settlement.project.videostats.repository.VideoStatsRepository;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

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
        return VideoStatsResponseDto.builder()
                .videoId(videoId)
                .views(views)
                .watchTime(watchTime)
                .period(period)
                .build();
    }

    public VideoStatsResponseDto getVideoStats(Long videoId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());
        VideoStats startStats = videoStatsRepository.findByVideoIdAndCreatedAt(videoId, dateRange.getStart())
                .orElseThrow(() -> new RuntimeException("Stats not found for video: " + videoId));
        VideoStats endStats = videoStatsRepository.findByVideoIdAndCreatedAt(videoId, dateRange.getEnd())
                .orElseThrow(() -> new RuntimeException("Stats not found for video: " + videoId));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for id: " + videoId));

        long views = endStats.getTotalViews() - startStats.getTotalViews();
        long watchTime = views * video.getPlayTime();

        return VideoStatsResponseDto.fromEntity(videoId, views, watchTime, period);
    }
}