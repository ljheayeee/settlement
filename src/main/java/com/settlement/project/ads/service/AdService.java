package com.settlement.project.ads.service;

import com.settlement.project.ads.dto.AdRequestDto;
import com.settlement.project.ads.dto.AdResponseDto;
import com.settlement.project.ads.entity.Ad;
import com.settlement.project.ads.repository.AdRepository;
import com.settlement.project.videoadstats.service.VideoAdStatsService;
import com.settlement.project.util.BatchProcessor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public AdService(AdRepository adRepository, VideoAdStatsService videoAdStatsService) {
        this.adRepository = adRepository;
        this.videoAdStatsService = videoAdStatsService;
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
        int resetCount;
        do {
            resetCount = adRepository.resetUsedAdsInBatch(BatchProcessor.BATCH_SIZE);
        } while (resetCount > 0);
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
