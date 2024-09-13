package com.settlement.project.main.videoadstats.service;

import com.settlement.project.common.ads.entity.Ad;
import com.settlement.project.common.ads.repository.AdRepository;
import com.settlement.project.main.video.exception.AdPlaybackException;
import com.settlement.project.main.videoadstats.dto.VideoAdStatsRequestDto;
import com.settlement.project.main.videoadstats.dto.PlayAdResponseDto;
import com.settlement.project.main.videoadstats.dto.VideoAdStatsResponseDto;
import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import com.settlement.project.common.videoadstats.repository.VideoAdStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoAdStatsService {
    private final VideoAdStatsRepository videoAdStatsRepository;
    private static final int AD_INTERVAL_SECONDS = 300; // 5분
    private final AdRepository adRepository;

    public VideoAdStatsService(VideoAdStatsRepository videoAdStatsRepository, AdRepository adRepository) {
        this.videoAdStatsRepository = videoAdStatsRepository;
        this.adRepository = adRepository;
    }

    @Transactional
    public void createStatsForVideoAds(Long videoId, List<Long> adIds) {
        List<VideoAdStats> stats = adIds.stream()
                .map(adId -> VideoAdStats.createNewStats(videoId, adId))
                .collect(Collectors.toList());

        videoAdStatsRepository.saveAll(stats);
    }

    public List<Long> getAdIdsForVideo(Long videoId) {
        return videoAdStatsRepository.findByVideoId(videoId).stream()
                .map(VideoAdStats::getAdId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementAdViewCount(Long videoId, Long adId) {
        VideoAdStats videoAdStats = videoAdStatsRepository.findByVideoIdAndAdId(videoId, adId)
                .orElseThrow(() -> new RuntimeException("Stats not found for video and ad"));
        videoAdStats.incrementDailyAdView();
        videoAdStatsRepository.save(videoAdStats);
    }

    public List<VideoAdStatsResponseDto> getStatsForVideo(Long videoId) {
        List<VideoAdStats> videoAdStatsList = videoAdStatsRepository.findByVideoId(videoId);
        return videoAdStatsList.stream()
                .map(VideoAdStatsResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoAdStatsResponseDto createOrUpdateStats(Long videoId, Long adId, VideoAdStatsRequestDto requestDto) {
        VideoAdStats videoAdStats = videoAdStatsRepository.findByVideoIdAndAdId(videoId, adId)
                .orElseGet(() -> VideoAdStats.createNewStats(videoId, adId));

        long newDailyAdView = videoAdStats.getDailyAdView() + requestDto.getDailyAdView();
        videoAdStats = VideoAdStats.builder()
                .videoId(videoId)
                .adId(adId)
                .dailyAdView(newDailyAdView)
                .totalAdView(videoAdStats.getTotalAdView())
                .build();

        VideoAdStats savedVideoAdStats = videoAdStatsRepository.save(videoAdStats);
        return VideoAdStatsResponseDto.fromEntity(savedVideoAdStats);
    }

    public List<VideoAdStats> findDailyStats(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return videoAdStatsRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }

    public List<VideoAdStats> getVideoAdStats(Long videoId) {
        return videoAdStatsRepository.findByVideoId(videoId);
    }

    public List<VideoAdStats> getVideoAdStatsForDate(Long videoId, LocalDate date) {
        return videoAdStatsRepository.findByVideoIdAndDate(videoId, date);
    }

    public long getPreviousDayAdViews(Long videoAdStatsId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return videoAdStatsRepository.findByIdAndDate(videoAdStatsId, yesterday)
                .map(VideoAdStats::getDailyAdView)
                .orElse(0L);
    }

    public Ad getAdById(Long adId) {
        return adRepository.getAdById(adId);
    }

    @Transactional
    public PlayAdResponseDto checkAndPlayAd(Long videoId, int watchHistoryTime) {
        try {
            int adIndex = watchHistoryTime / AD_INTERVAL_SECONDS;
            List<Long> adIds = getAdIdsForVideo(videoId);

            if (watchHistoryTime % AD_INTERVAL_SECONDS == 0 && adIndex > 0 && adIndex <= adIds.size()) {
                Long adId = adIds.get(adIndex - 1);
                incrementAdViewCount(videoId, adId);

                Ad ad = getAdById(adId);
                log.info("Ad played for video: {}, ad: {}", videoId, adId);
                return PlayAdResponseDto.fromEntity(ad);
            }
        } catch (Exception e) {
            log.error("Error checking and playing ad for video: {}", videoId, e);
            throw new AdPlaybackException("Failed to check and play ad", e);
        }
        return null;
    }

    public List<VideoAdStats> getVideoAdStatsForDateRange(Long videoId, LocalDate startDate, LocalDate endDate) {
        return videoAdStatsRepository.findByVideoIdAndCreatedAtBetween(
                videoId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    @Transactional
    public void updateTotalAndResetDailyViews() {
        List<VideoAdStats> allStats = videoAdStatsRepository.findAll();
        for (VideoAdStats stat : allStats) {
            stat.updateTotalAndResetDaily();
            videoAdStatsRepository.save(stat);
        }
    }

    @Transactional
    public void updateTotalAdView(Long videoId, Long adId) {
        Optional<VideoAdStats> optionalAdStats = videoAdStatsRepository.findByVideoIdAndAdId(videoId, adId);
        if (optionalAdStats.isPresent()) {
            VideoAdStats adStats = optionalAdStats.get();
            adStats.updateTotalAdView();
            videoAdStatsRepository.save(adStats);
        }
    }
}