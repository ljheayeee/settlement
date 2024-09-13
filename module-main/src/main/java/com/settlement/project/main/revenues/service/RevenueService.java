package com.settlement.project.main.revenues.service;

import com.settlement.project.common.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.common.util.DateRange;
import com.settlement.project.common.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.common.revenues.entity.Revenue;
import com.settlement.project.common.revenues.repository.RevenueRepository;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.main.video.service.VideoService;
import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import com.settlement.project.common.videoadstats.repository.VideoAdStatsRepository;
import com.settlement.project.main.videoadstats.service.VideoAdStatsService;
import com.settlement.project.common.videostats.entity.VideoStats;
import com.settlement.project.common.videostats.repository.VideoStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RevenueService {

    private static final long[] VIDEO_VIEW_THRESHOLDS = {100000L, 500000L, 1000000L};
    private static final long[] VIDEO_RATES = {100L, 110L, 130L, 150L};
    private static final long[] AD_VIEW_THRESHOLDS = {100000L, 500000L, 1000000L};
    private static final long[] AD_RATES = {1000L, 1200L, 1500L, 2000L};

    private final RevenueRepository revenueRepository;
    private final VideoStatsRepository videoStatsRepository;
    private final VideoAdStatsRepository videoAdStatsRepository;
    private final VideoRepository videoRepository;
    private final VideoAdStatsService videoAdStatsService;

    public RevenueService(RevenueRepository revenueRepository,
                          VideoStatsRepository videoStatsRepository,
                          VideoAdStatsRepository videoAdStatsRepository,
                          VideoRepository videoRepository, VideoAdStatsService videoAdStatsService) {
        this.revenueRepository = revenueRepository;
        this.videoStatsRepository = videoStatsRepository;
        this.videoAdStatsRepository = videoAdStatsRepository;
        this.videoRepository = videoRepository;
        this.videoAdStatsService = videoAdStatsService;
    }

    @Transactional
    public void calculateDailyRevenue(RevenueCalculationRequestDto requestDto) {
        LocalDate calculationDate = requestDto.getCalculationDate();

        // 해당 날짜에 대한 수익이 이미 계산되었는지 확인
        List<Revenue> existingRevenues = revenueRepository.findByCreatedAtBetween(
                calculationDate.atStartOfDay(), calculationDate.plusDays(1).atStartOfDay());

        if (!existingRevenues.isEmpty()) {
            log.info("Revenue already calculated for date: {}", calculationDate);
            return;  // 이미 계산된 경우 작업을 중단
        }

        // 모든 비디오 가져오기
        List<VideoStats> videoStatsList = videoStatsRepository.findByCreatedDate(calculationDate);

        // 중복 체크를 위한 Set 사용
        Set<Long> processedVideos = new HashSet<>();

        for (VideoStats videoStats : videoStatsList) {
            // 이미 처리된 비디오는 스킵
            if (processedVideos.contains(videoStats.getVideoId())) {
                continue;
            }

            Video video = videoRepository.findById(videoStats.getVideoId())
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            long dailyViews = videoStats.getDailyViews();
            long totalViews = videoStats.getTotalViews();

            long videoRevenue = calculateRevenue(totalViews, dailyViews, VIDEO_VIEW_THRESHOLDS, VIDEO_RATES);
            long adRevenue = calculateTotalAdRevenue(videoStats.getVideoId(), video.getPlayTime());

            long totalRevenue = videoRevenue + adRevenue;

            log.info("Video ID: {}, Daily Views: {}, Total Views: {}, Video Revenue: {}, Ad Revenue: {}",
                    videoStats.getVideoId(), dailyViews, totalViews, videoRevenue, adRevenue);

            Revenue revenue = Revenue.builder()
                    .userId(videoStats.getUserId())
                    .videoId(videoStats.getVideoId())
                    .revenueVideo(videoRevenue)
                    .revenueAd(adRevenue)
                    .revenueTotal(totalRevenue)
                    .build();

            revenueRepository.save(revenue);

            // 비디오 ID를 Set에 추가하여 중복 방지
            processedVideos.add(videoStats.getVideoId());
        }
        videoAdStatsService.updateTotalAndResetDailyViews();

    }

    private long calculateRevenue(long totalViews, long dailyViews, long[] thresholds, long[] rates) {
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

        return revenue / 100; // 1원 단위로 변환 및 절사
    }

    private long calculateTotalAdRevenue(Long videoId, int playTime) {
        long totalAdRevenue = 0;

        if (playTime >= 300) {  // 5분(300초) 이상 영상에 대해서만 광고 수익 계산
            // 날짜에 관계없이 비디오 ID에 해당하는 모든 광고 데이터를 가져옵니다.
            List<VideoAdStats> adStatsList = videoAdStatsRepository.findByVideoId(videoId);

            for (VideoAdStats adStats : adStatsList) {
                long adViews = adStats.getDailyAdView(); // 광고의 조회수를 가져옴
                long adRevenue = calculateRevenue(adViews, adViews, AD_VIEW_THRESHOLDS, AD_RATES);
                totalAdRevenue += adRevenue;
                log.info("Video ID: {}, Ad ID: {}, Ad Views: {}, Ad Revenue: {}",
                        videoId, adStats.getAdId(), adViews, adRevenue);
            }
        }

        return totalAdRevenue;
    }



    @Transactional(readOnly = true)
    public UserRevenueDetailResponseDto getUserRevenueDetail(Long userId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());
        List<Revenue> revenues = revenueRepository.findByUserIdAndCreatedAtBetween(
                userId,
                dateRange.getStart().atStartOfDay(),
                dateRange.getEnd().atTime(23, 59, 59)
        );

        Map<Long, String> videoTitleMap = videoRepository.findTitlesByVideoIds(
                revenues.stream().map(Revenue::getVideoId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(VideoRepository.VideoTitleProjection::getId, VideoRepository.VideoTitleProjection::getTitle));

        return UserRevenueDetailResponseDto.fromEntity(revenues, period, videoTitleMap);
    }

    @Transactional(readOnly = true)
    public List<Revenue> getRevenuesByDate(LocalDate date) {
        return revenueRepository.findByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
