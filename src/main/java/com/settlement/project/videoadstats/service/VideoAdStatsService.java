package com.settlement.project.videoadstats.service;

import com.settlement.project.ads.repository.AdRepository;
import com.settlement.project.user.entity.User;
import com.settlement.project.video.entity.Video;
import com.settlement.project.videoadstats.dto.PlayAdResponseDto;
import com.settlement.project.ads.entity.Ad;
import com.settlement.project.ads.service.AdService;
import com.settlement.project.video.exception.AdPlaybackException;
import com.settlement.project.videoadstats.dto.VideoAdStatsRequestDto;
import com.settlement.project.videoadstats.dto.VideoAdStatsResponseDto;
import com.settlement.project.videoadstats.entity.VideoAdStats;
import com.settlement.project.videoadstats.repository.VideoAdStatsRepository;
import com.settlement.project.videostats.entity.VideoStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        videoAdStats.incrementAdView();
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
        VideoAdStats existingVideoAdStats = videoAdStatsRepository.findByVideoIdAndAdId(videoId, adId)
                .orElse(null);

        VideoAdStats videoAdStats;
        if (existingVideoAdStats == null) {
            videoAdStats = requestDto.toEntity(videoId, adId,requestDto.getStatsAdView());
        } else {
            videoAdStats = existingVideoAdStats;
            // 기존 통계에 새로운 조회수를 더합니다.
            videoAdStats.updateStatsAdView( (existingVideoAdStats.getStatsAdView() + requestDto.getStatsAdView()));
        }

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



        // 기존 메서드들...

    public long getPreviousDayAdViews(Long videoAdStatsId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return videoAdStatsRepository.findByIdAndDate(videoAdStatsId, yesterday)
                .map(stats -> (long) stats.getStatsAdView())  // Integer를 long으로 변환
                .orElse(0L);
    }


    public Ad getAdById(Long adId) {
        return adRepository.getAdById(adId);
    }

    @Transactional
    public PlayAdResponseDto checkAndPlayAd(Long videoId, int watchHistoryTime) {
        try {
            int adIndex = watchHistoryTime / AD_INTERVAL_SECONDS;
            List<Long> adIds = getAdIdsForVideo(videoId);  // 비디오에 할당된 광고 ID 목록 가져오기

            if (watchHistoryTime % AD_INTERVAL_SECONDS == 0 && adIndex > 0 && adIndex <= adIds.size()) {
                Long adId = adIds.get(adIndex - 1);
                incrementAdViewCount(videoId, adId);  // 광고 시청 횟수 증가

                // AdService를 통해 광고 정보를 가져옴
                Ad ad = getAdById(adId);
                log.info("Ad played for video: {}, ad: {}", videoId, adId);
                return PlayAdResponseDto.fromEntity(ad);
            }
        } catch (Exception e) {
            log.error("Error checking and playing ad for video: {}", videoId, e);
            throw new AdPlaybackException("Failed to check and play ad", e);
        }
        return null;  // 광고가 재생되지 않은 경우 null 반환
    }


    public Long getOrCreateVideoAdStatsId(Long videoId) {
    return null;
    }

    public List<VideoAdStats> getVideoAdStatsForDateRange(Long videoId, LocalDate startDate, LocalDate endDate) {
        return videoAdStatsRepository.findByVideoIdAndCreatedAtBetween(
                videoId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }
}