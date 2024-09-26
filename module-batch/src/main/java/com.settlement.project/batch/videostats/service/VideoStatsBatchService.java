package com.settlement.project.batch.videostats.service;


import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.common.videostats.entity.VideoStats;
import com.settlement.project.common.videostats.repository.VideoStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
public class VideoStatsBatchService {

    private final VideoStatsRepository videoStatsRepository;
    private final VideoRepository videoRepository;

    public VideoStatsBatchService(VideoStatsRepository videoStatsRepository, VideoRepository videoRepository) {
        this.videoStatsRepository = videoStatsRepository;
        this.videoRepository = videoRepository;
    }

    ///-----
    @Transactional(readOnly = true)
    public List<Long> getActiveVideoIds() {
        return videoRepository.findAllActiveVideoIds();
    }

    @Transactional
    public VideoStats processVideoStats(Long videoId, LocalDate date) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for id: " + videoId));

        long totalViews = video.getView();
        long totalWatchTime = totalViews * video.getPlayTime();

        // LocalDate previousDate = date.minusDays(1);
        // VideoStats previousStats = videoStatsRepository.findLatestStatsByVideoIdAndDate(videoId, previousDate)
        //         .orElse(null);

        // 전날 통계를 가져옴 (date는 이미 전날 날짜임)
        VideoStats previousStats = videoStatsRepository.findLatestStatsByVideoIdAndDate(videoId, date)
                .orElse(null);

        VideoStats currentStats = videoStatsRepository.findLatestStatsByVideoIdAndDate(videoId, date)
                .orElseGet(() -> VideoStats.builder()
                        .videoId(videoId)
                        .userId(video.getUser().getId())
                        .totalViews(0L)
                        .totalWatchTime(0L)
                        .dailyViews(0L)
                        .dailyWatchTime(0L)
                        .build());

        currentStats.updateStats(totalViews, totalWatchTime, previousStats);

        log.info("Processed stats for video {} on date {}: dailyViews={}, dailyWatchTime={}",
                videoId, date, currentStats.getDailyViews(), currentStats.getDailyWatchTime());

        return currentStats;
    }


    @Transactional
    public void saveVideoStats(List<? extends VideoStats> statsToSave) {
        videoStatsRepository.saveAll(statsToSave);
        log.info("Saved {} video stats", statsToSave.size());
    }

}
