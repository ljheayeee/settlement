package com.settlement.project.main.ads.service;

import com.settlement.project.common.ads.dto.AdRequestDto;
import com.settlement.project.common.ads.dto.AdResponseDto;
import com.settlement.project.common.ads.entity.Ad;
import com.settlement.project.common.ads.repository.AdRepository;
import com.settlement.project.common.util.BatchProcessor;
import com.settlement.project.main.videoadstats.service.VideoAdStatsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class AdService {

    private static final int AD_INTERVAL_MINUTES = 5;
    private static final Logger log = LoggerFactory.getLogger(AdService.class);
    private final AdRepository adRepository;
    private final VideoAdStatsService videoAdStatsService;
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY = 100; // milliseconds

    public AdService(AdRepository adRepository, VideoAdStatsService videoAdStatsService) {
        this.adRepository = adRepository;
        this.videoAdStatsService = videoAdStatsService;
    }

    @Transactional
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,  // 최대 3번까지 재시도
            backoff = @Backoff(delay = 500)  // 500ms의 지연 후 재시도
    )
    public void assignAdsToVideo(Long videoId, int videoLengthMinutes) {
        if (videoLengthMinutes <= AD_INTERVAL_MINUTES) {
            log.info("Video too short for ads. Video ID: {}, Length: {} minutes", videoId, videoLengthMinutes);
            return;
        }
        int requiredAdCount = (videoLengthMinutes - 1) / AD_INTERVAL_MINUTES;
        try {
            List<Ad> selectedAds = selectAds(requiredAdCount);
            List<Long> adIds = selectedAds.stream().map(Ad::getId).collect(Collectors.toList());
            videoAdStatsService.createStatsForVideoAds(videoId, adIds);
            log.info("Successfully assigned {} ads to video: {}", requiredAdCount, videoId);
        } catch (Exception e) {
            log.error("Failed to assign ads to video: {}", videoId, e);
            throw e;
        }
    }




    @Transactional
    public AdResponseDto createAd(AdRequestDto requestDto) {
        Ad ad = requestDto.toEntity();
        ad.updateStatus(LocalDate.now());
        Ad savedAd = adRepository.save(ad);
        return AdResponseDto.fromEntity(savedAd);
    }
    @Transactional

    public AdResponseDto updateAd(Long id, AdRequestDto requestDto) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ad not found with id: " + id));
        ad.update(requestDto.getAdUrl(), requestDto.getStartDate(), requestDto.getEndDate(), requestDto.getAdPlaytime());
        ad.updateStatus(LocalDate.now());
        Ad updatedAd = adRepository.save(ad);
        return AdResponseDto.fromEntity(updatedAd);
    }
    @Transactional
    public void softDeleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ad not found with id: " + id));
        ad.softDelete();
        adRepository.save(ad);
    }




    private List<Ad> selectAds(int count) {
        List<Ad> selectedAds = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        while (selectedAds.size() < count) {
            int remainingCount = count - selectedAds.size();
            int batchSize = Math.min(remainingCount, BatchProcessor.BATCH_SIZE);
            List<Ad> batchAds = adRepository.findRandomUnusedActiveAds(batchSize);
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
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                int resetCount;
                do {
                    resetCount = adRepository.resetUsedAdsInBatch(BatchProcessor.BATCH_SIZE);
                } while (resetCount > 0);
                break; // Success, exit the loop
            } catch (CannotAcquireLockException e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw e; // Rethrow if max retries reached
                }
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying", ie);
                }
            }
        }
    }

    public Ad getAdById(Long adId) {
        return adRepository.findById(adId)
                .orElseThrow(() -> new EntityNotFoundException("Ad not found with id: " + adId));
    }


    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    @Transactional
    public void updateAdStatuses() {
        LocalDate currentDate = LocalDate.now();
        List<Ad> ads = adRepository.findAll();
        for (Ad ad : ads) {
            ad.updateStatus(currentDate);
        }
        adRepository.saveAll(ads);
        log.info("Updated ad statuses at {}", currentDate);
    }





}
