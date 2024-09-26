package com.settlement.project.main.videostats.service;

import com.settlement.project.common.util.DateRange;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.common.videostats.dto.TopVideosByViewsResponseDto;
import com.settlement.project.common.videostats.dto.TopVideosByWatchTimeResponseDto;
import com.settlement.project.common.videostats.dto.VideoStatsResponseDto;
import com.settlement.project.common.videostats.entity.VideoStats;
import com.settlement.project.common.videostats.repository.VideoStatsRepository;
import com.settlement.project.common.watchhistory.repository.WatchHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class VideoStatsService {

    private final VideoStatsRepository videoStatsRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoRepository videoRepository;

    public VideoStatsService(VideoStatsRepository videoStatsRepository,
                             WatchHistoryRepository watchHistoryRepository, VideoRepository videoRepository) {
        this.videoStatsRepository = videoStatsRepository;
        this.watchHistoryRepository = watchHistoryRepository;
        this.videoRepository = videoRepository;
    }


    public List<TopVideosByViewsResponseDto> getTop5VideosByViews(Long userId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());
        List<VideoStats> videoStats = videoStatsRepository.findTop5ByViewsInDateRange(
                userId, dateRange.getStart(), dateRange.getEnd());

        return videoStats.stream()
                .map(stats -> TopVideosByViewsResponseDto.builder()
                        .videoId(stats.getVideoId())
                        .totalViews(stats.getTotalViews())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TopVideosByWatchTimeResponseDto> getTop5VideosByWatchTime(Long userId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());
        List<VideoStats> videoStats = videoStatsRepository.findTop5ByWatchTimeInDateRange(
                userId, dateRange.getStart(), dateRange.getEnd());

        return videoStats.stream()
                .map(stats -> TopVideosByWatchTimeResponseDto.builder()
                        .videoId(stats.getVideoId())
                        .totalWatchTime(stats.getTotalWatchTime())
                        .build())
                .collect(Collectors.toList());
    }

    public VideoStatsResponseDto getVideoStats(Long videoId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());  // 종료일이 현재 날짜로 제한된 DateRange
        VideoStats startStats = videoStatsRepository.findByVideoIdAndCreatedAt(videoId, dateRange.getStart())
                .orElseThrow(() -> new RuntimeException("Stats not found for video: " + videoId));
        VideoStats endStats = videoStatsRepository.findByVideoIdAndCreatedAt(videoId, dateRange.getEnd())
                .orElseThrow(() -> new RuntimeException("Stats not found for video: " + videoId));

        long views = endStats.getTotalViews() - startStats.getTotalViews();
        long watchTime = endStats.getTotalWatchTime() - startStats.getTotalWatchTime();

        return VideoStatsResponseDto.fromEntity(videoId, views, watchTime, period);
    }



}
