package com.settlement.video.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private static final int AD_INTERVAL_MINUTES = 5;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final StatsRepository statsRepository;
    private static final int BATCH_SIZE = 500;

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






    /* create video 관련 메소드  start  */
    private void assignAdsToVideo(Video video) {
        int videoLengthMinutes = video.getPlayTimeInMinutes();
        if (videoLengthMinutes <= AD_INTERVAL_MINUTES) {
            log.info("비디오가 광고 삽입하기에 너무 짧습니다. 비디오 ID: {}, 길이: {}분", video.getId(), videoLengthMinutes);
            return;
        }

        int numberOfAdsNeeded = (videoLengthMinutes - 1) / AD_INTERVAL_MINUTES;
        log.info("비디오 ID: {}에 {}개의 광고를 할당합니다", video.getId(), numberOfAdsNeeded);

        List<Ad> selectedAds = selectAds(numberOfAdsNeeded);
        List<Stats> statsToSave = new ArrayList<>();

        for (Ad ad : selectedAds) {
            Stats stats = Stats.builder()
                    .video(video)
                    .ad(ad)
                    .statsAdView(0)
                    .build();
            statsToSave.add(stats);
            ad.markAsUsed();
        }

        // Batch save selected ads
        saveBatch(selectedAds, adRepository::saveAll);

        // Batch save stats
        saveBatch(statsToSave, statsRepository::saveAll);
    }

    private <T> void saveBatch(List<T> items, java.util.function.Consumer<List<T>> saveFunction) {
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<T> batch = items.subList(i, Math.min(items.size(), i + BATCH_SIZE));
            saveFunction.accept(batch);
        }
    }

    private List<Ad> selectAds(int count) {
        List<Ad> selectedAds = new ArrayList<>();

        while (selectedAds.size() < count) {
            int remainingCount = count - selectedAds.size();
            int batchSize = Math.min(remainingCount, BATCH_SIZE);

            List<Ad> batchAds = adRepository.findRandomUnusedAds(batchSize);

            if (batchAds.isEmpty()) {
                resetAdsInBatches(BATCH_SIZE);
                continue;
            }

            for (Ad ad : batchAds) {
                ad.markAsUsed();
                selectedAds.add(ad);
                if (selectedAds.size() == count) break;
            }
        }

        return selectedAds;
    }

    private int resetAdsInBatches(int batchSize) {
        int totalReset = 0;
        int resetInBatch;
        do {
            resetInBatch = adRepository.resetUsedAdsInBatch(batchSize);
            totalReset += resetInBatch;
        } while (resetInBatch > 0);
        return totalReset;
    }
    /* create video 관련 메소드  end  */
}









