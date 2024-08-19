package com.settlement.project.ads.service;

import com.settlement.project.ads.entity.Ad;
import com.settlement.project.video.entity.Video;
import com.settlement.project.ads.repository.AdRepository;
import com.settlement.project.stats.service.StatsService;
import com.settlement.project.util.BatchProcessor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdService {
    private static final int AD_INTERVAL_MINUTES = 5;
    private static final Logger log = LoggerFactory.getLogger(AdService.class);
    private final AdRepository adRepository;
    private final StatsService statsService;

    public AdService(AdRepository adRepository, StatsService statsService) {
        this.adRepository = adRepository;
        this.statsService = statsService;
    }
    @Transactional
    public void assignAdsToVideo(Long videoId, int videoLengthMinutes) {
        if (videoLengthMinutes <= AD_INTERVAL_MINUTES) {
            log.info("Video too short for ads. Video ID: {}, Length: {} minutes", videoId, videoLengthMinutes);
            return;
        }
        int requiredAdCount = (videoLengthMinutes - 1) / AD_INTERVAL_MINUTES;
        try {
            List<Ad> selectedAds = selectAds(requiredAdCount);
            List<Long> adIds = selectedAds.stream().map(Ad::getId).collect(Collectors.toList());
            statsService.createStatsForVideoAds(videoId, adIds);
            log.info("Successfully assigned {} ads to video: {}", requiredAdCount, videoId);
        } catch (Exception e) {
            log.error("Failed to assign ads to video: {}", videoId, e);
            throw e;
        }
    }

    private List<Ad> selectAds(int count) {
        List<Ad> selectedAds = new ArrayList<>();
        while (selectedAds.size() < count) {
            int remainingCount = count - selectedAds.size();
            int batchSize = Math.min(remainingCount, BatchProcessor.BATCH_SIZE);
            List<Ad> batchAds = adRepository.findRandomUnusedAds(batchSize);
            if (batchAds.isEmpty()) {
                resetUsedAds();
                continue;
            }
            for (Ad ad : batchAds) {
                ad.markAsUsed();
                selectedAds.add(ad);
                if (selectedAds.size() == count) break;
            }
        }
        BatchProcessor.saveBatch(selectedAds, adRepository::saveAll);
        return selectedAds;
    }


    @Transactional
    public void resetUsedAds() {
        int resetCount;
        do {
            resetCount = adRepository.resetUsedAdsInBatch(BatchProcessor.BATCH_SIZE);
        } while (resetCount > 0);
    }


    public Ad getAdById(Long adId) {
        return adRepository.findById(adId)
                .orElseThrow(() -> new EntityNotFoundException("Ad not found with id: " + adId));
    }

    public List<Ad> getAdsByIds(List<Long> adIds) {
        return adRepository.findAllById(adIds);
    }

}
