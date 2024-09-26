package com.settlement.project.batch.revenues.service;


import com.settlement.project.common.revenues.entity.Revenue;
import com.settlement.project.common.revenues.repository.RevenueRepository;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import com.settlement.project.common.videoadstats.repository.VideoAdStatsRepository;
import com.settlement.project.common.videostats.entity.VideoStats;
import com.settlement.project.common.videostats.repository.VideoStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class RevenueBatchService {

    private static final long[] VIDEO_VIEW_THRESHOLDS = {100000L, 500000L, 1000000L};
    private static final long[] VIDEO_RATES = {100L, 110L, 130L, 150L};
    private static final long[] AD_VIEW_THRESHOLDS = {100000L, 500000L, 1000000L};
    private static final long[] AD_RATES = {1000L, 1200L, 1500L, 2000L};

    private final RevenueRepository revenueRepository;
    private final VideoStatsRepository videoStatsRepository;
    private final VideoAdStatsRepository videoAdStatsRepository;
    private final VideoRepository videoRepository;


    public RevenueBatchService(RevenueRepository revenueRepository,
                               VideoStatsRepository videoStatsRepository,
                               VideoAdStatsRepository videoAdStatsRepository,
                               VideoRepository videoRepository) {
        this.revenueRepository = revenueRepository;
        this.videoStatsRepository = videoStatsRepository;
        this.videoAdStatsRepository = videoAdStatsRepository;
        this.videoRepository = videoRepository;
    }

    // 청크 기반 처리 로직 추가
    @Transactional(readOnly = true)
    public List<VideoStats> getVideoStatsForDate(LocalDate calculationDate) {
        // 해당 날짜의 모든 비디오 통계를 가져옴
        return videoStatsRepository.findByCreatedDate(calculationDate);
    }

    @Transactional
    public Revenue calculateRevenue(VideoStats videoStats) {
        Video video = videoRepository.findById(videoStats.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        long dailyViews = videoStats.getDailyViews();
        long totalViews = videoStats.getTotalViews();

        // 동영상 정산 금액 계산
        long videoRevenue = calculateRevenueAmount(totalViews, dailyViews, VIDEO_VIEW_THRESHOLDS, VIDEO_RATES);

        // 광고 정산 금액 계산
        long adRevenue = calculateTotalAdRevenue(videoStats.getVideoId(), video.getPlayTime());

        // 전체 정산 금액 계산
        long totalRevenue = videoRevenue + adRevenue;

        log.info("Video ID: {}, Daily Views: {}, Total Views: {}, Video Revenue: {}, Ad Revenue: {}",
                videoStats.getVideoId(), dailyViews, totalViews, videoRevenue, adRevenue);

        // 정산 결과를 Revenue 엔티티로 변환하여 반환
        return Revenue.builder()
                .userId(videoStats.getUserId())
                .videoId(videoStats.getVideoId())
                .revenueVideo(videoRevenue)
                .revenueAd(adRevenue)
                .revenueTotal(totalRevenue)
                .build();
    }

    @Transactional
    public void saveRevenues(List<? extends Revenue> revenues) {
        revenueRepository.saveAll(revenues);
        log.info("Saved {} revenue records", revenues.size());
    }

    private long calculateRevenueAmount(long totalViews, long dailyViews, long[] thresholds, long[] rates) {
        long revenue = 0;
        long previousViews = totalViews - dailyViews;
        long remainingViews = dailyViews;

        for (int i = 0; i < thresholds.length; i++) {
            long thresholdEnd = thresholds[i];
            long rate = rates[i];

            if (previousViews >= thresholdEnd) {
                continue;
            }

            long viewsInThisRange = Math.min(remainingViews, thresholdEnd - previousViews);
            revenue += viewsInThisRange * rate;
            previousViews += viewsInThisRange;
            remainingViews -= viewsInThisRange;

            if (remainingViews == 0) break;
        }

        if (remainingViews > 0) {
            revenue += remainingViews * rates[rates.length - 1];
        }

        return revenue / 100;  // 1원 단위로 절사
    }

    private long calculateTotalAdRevenue(Long videoId, int playTime) {
        log.info("Calculating ad revenue for Video ID: {}, Play Time: {}", videoId, playTime);
        long totalAdRevenue = 0;

        if (playTime >= 300) {
            try {
                List<VideoAdStats> adStatsList = videoAdStatsRepository.findByVideoId(videoId);
                log.info("Video ID: {}, Number of Ad Stats: {}", videoId, adStatsList.size());

                for (VideoAdStats adStats : adStatsList) {
                    long adViews = adStats.getDailyAdView();
                    log.info("Ad ID: {}, Daily Ad Views: {}", adStats.getAdId(), adViews);

                    long adRevenue = calculateRevenueAmount(adViews, adViews, AD_VIEW_THRESHOLDS, AD_RATES);
                    totalAdRevenue += adRevenue;
                    log.info("Video ID: {}, Ad ID: {}, Ad Views: {}, Ad Revenue: {}",
                            videoId, adStats.getAdId(), adViews, adRevenue);
                }
            } catch (Exception e) {
                log.error("Error calculating ad revenue for Video ID: {}", videoId, e);
            }
        } else {
            log.info("Video ID: {} is not eligible for ad revenue (Play Time < 300 seconds)", videoId);
        }

        log.info("Total Ad Revenue for Video ID: {}: {}", videoId, totalAdRevenue);
        return totalAdRevenue;
    }
}
