package com.settlement.project.main.videostats.service;

import com.settlement.project.common.util.DateRange;
import com.settlement.project.common.video.entity.Video;
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
import java.util.Optional;
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

    @Transactional
    public void updateDailyStats(LocalDate date) {
        List<Long> activeVideoIds = videoRepository.findAllActiveVideoIds();
        LocalDate previousDate = date.minusDays(1);

        for (Long videoId : activeVideoIds) {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found for id: " + videoId));
            long totalViews = video.getView();
            long totalWatchTime = totalViews * video.getPlayTime();

            Optional<VideoStats> previousStats = videoStatsRepository.findLatestStatsByVideoIdAndDate(videoId, previousDate);
            VideoStats currentStats = videoStatsRepository.findLatestStatsByVideoIdAndDate(videoId, date)
                    .orElseGet(() -> VideoStats.builder()
                            .videoId(videoId)
                            .userId(video.getUser().getId())
                            .totalViews(0)
                            .totalWatchTime(0)
                            .dailyViews(0)
                            .dailyWatchTime(0)
                            .build());

            currentStats.updateStats(totalViews, totalWatchTime, previousStats.orElse(null));
            videoStatsRepository.save(currentStats);

            log.info("Updated daily stats for video {}: views={}, dailyWatchTime={}",
                    videoId, currentStats.getDailyViews(), currentStats.getDailyWatchTime());
        }
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

    @Transactional
    public void createInitialVideoStats(Long videoId, Long userId) {
        VideoStats initialStats = VideoStats.builder()
                .videoId(videoId)
                .userId(userId)
                .totalViews(0L)
                .totalWatchTime(0L)
                .dailyViews(0L)
                .dailyWatchTime(0L)
                .build();
        videoStatsRepository.save(initialStats);
        log.debug("Initial VideoStats created for video: {}", videoId);
    }

    private long calculateViewsForPeriod(VideoStats endStats, DateRange dateRange) {
        VideoStats startStats = videoStatsRepository.findByVideoIdAndCreatedAt(endStats.getVideoId(), dateRange.getStart())
                .orElse(VideoStats.builder()
                        .videoId(endStats.getVideoId())
                        .userId(endStats.getUserId())
                        .totalViews(0L)
                        .totalWatchTime(0L)
                        .dailyViews(0L)
                        .dailyWatchTime(0L)
                        .build());
        return endStats.getTotalViews() - startStats.getTotalViews();
    }

    private long calculateWatchTimeForPeriod(VideoStats endStats, Video video, DateRange dateRange) {
        long viewsInPeriod = calculateViewsForPeriod(endStats, dateRange);  // 위 메서드 재사용
        return viewsInPeriod * video.getPlayTime();
    }


}
