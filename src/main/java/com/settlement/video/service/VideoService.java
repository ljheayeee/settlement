package com.settlement.video.service;

import com.settlement.video.config.AdBatchConfig;
import com.settlement.video.dto.VideoRequestDto;
import com.settlement.video.dto.VideoResponseDto;
import com.settlement.video.entity.Ad;
import com.settlement.video.entity.Stats;
import com.settlement.video.entity.User;
import com.settlement.video.entity.Video;
import com.settlement.video.repository.AdRepository;
import com.settlement.video.repository.StatsRepository;
import com.settlement.video.repository.UserRepository;
import com.settlement.video.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private static final int AD_INTERVAL_MINUTES = 5;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final StatsRepository statsRepository;
    private static final int BATCH_SIZE = 1000;

    public VideoService(VideoRepository videoRepository, UserRepository userRepository,
                        AdRepository adRepository, StatsRepository statsRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.statsRepository = statsRepository;
    }
    //동영상 등록
    @Transactional
    public VideoResponseDto createVideo(VideoRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestDto.getUserId()));

        Video video = requestDto.toEntity(user);
        video = videoRepository.save(video);

        assignAdsToVideo(video);
        return VideoResponseDto.fromEntity(video);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
    }

    @Transactional
    public Video updateVideo(Long id, Video videoDetails) {
        Video video = getVideoById(id);
        // Update video details
        return videoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }


    //
    private void assignAdsToVideo(Video video) {
        int videoLengthMinutes = video.getPlayTimeInMinutes();
        if (videoLengthMinutes <= AD_INTERVAL_MINUTES) {
            log.info("비디오가 광고 삽입하기에 너무 짧습니다. 길이: {}분", videoLengthMinutes);
            return;
        }

        int numberOfAdsNeeded = (videoLengthMinutes - 1) / AD_INTERVAL_MINUTES;
        log.info("비디오 ID: {}에 {}개의 광고를 할당합니다", video.getId(), numberOfAdsNeeded);

        List<Ad> selectedAds = selectAds(numberOfAdsNeeded);
        for (Ad ad : selectedAds) {
            createStats(video, ad);
            ad.markAsUsed();
            adRepository.save(ad);
        }
    }

    private List<Ad> selectAds(int count) {
        List<Ad> unusedAds = adRepository.findByIsUsedFalse();
        List<Ad> allAds = adRepository.findAll();

        if (allAds.isEmpty()) {
            log.warn("시스템에 등록된 광고가 없습니다.");
            return Collections.emptyList();
        }

        List<Ad> selectedAds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (unusedAds.isEmpty()) {
                // 모든 광고가 사용되었다면 리셋
                allAds.forEach(Ad::resetUsage);
                adRepository.saveAll(allAds);
                unusedAds = new ArrayList<>(allAds);
            }

            Ad selectedAd = unusedAds.remove(0);
            selectedAds.add(selectedAd);
        }

        return selectedAds;
    }

    private void createStats(Video video, Ad ad) {
        Stats stats = Stats.builder()
                .video(video)
                .ad(ad)
                .statsAdView(0)
                .build();
        statsRepository.save(stats);
        log.info("통계 생성 완료: 비디오 ID: {}, 광고 ID: {}", video.getId(), ad.getId());
    }

//    @Transactional
//    public List<Ad> selectAdsOptimized(int count) {
//        long totalAdCount = adRepository.count();
//        if (totalAdCount == 0) {
//            log.warn("시스템에 등록된 광고가 없습니다.");
//            return Collections.emptyList();
//        }
//
//        List<Ad> selectedAds = new ArrayList<>();
//        Set<Long> selectedIds = new HashSet<>();
//
//        while (selectedAds.size() < count) {
//            int remainingCount = count - selectedAds.size();
//            int batchSize = Math.min(remainingCount, BATCH_SIZE);
//
//            List<Ad> batchAds = adRepository.findRandomUnusedAds(batchSize);
//
//            if (batchAds.isEmpty()) {
//                resetAllAdsUsage();
//                continue;
//            }
//
//            for (Ad ad : batchAds) {
//                if (selectedIds.add(ad.getId())) {
//                    ad.markAsUsed();
//                    selectedAds.add(ad);
//                }
//                if (selectedAds.size() == count) break;
//            }
//        }
//
//        adRepository.saveAll(selectedAds);
//        return selectedAds;
//    }
//
//    @Transactional
//    public void resetAllAdsUsage() {
//        int updatedRows = 0;
//        do {
//            updatedRows = adRepository.resetUsedAdsInBatch(BATCH_SIZE);
//        } while (updatedRows > 0);
//    }
}









