package com.settlement.project.main.dummyData;

import com.settlement.project.common.ads.entity.Ad;
import com.settlement.project.common.ads.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AdDummyDataGeneratorService {

    @Autowired
    private AdRepository adRepository;

    private static final int TOTAL_ADS = 200;
    private static final double ACTIVE_PERCENTAGE = 0.90; // 90% active
    private static final Random RANDOM = new Random();

    @Transactional
    public String generateDummyAds() {
        List<Ad> ads = new ArrayList<>();
        LocalDate today = LocalDate.now();

        int activeAdsCount = (int) (TOTAL_ADS * ACTIVE_PERCENTAGE);
        int scheduledAdsCount = (TOTAL_ADS - activeAdsCount) / 2;
        int expiredAdsCount = TOTAL_ADS - activeAdsCount - scheduledAdsCount;

        // Create Active Ads (90%)
        for (int i = 0; i < activeAdsCount; i++) {
            LocalDate startDate = today.minusDays(RANDOM.nextInt(30));  // 최대 30일 전부터 활성
            LocalDate endDate = today.plusDays(RANDOM.nextInt(30));  // 최대 30일 후까지 활성
            Ad ad = Ad.builder()
                    .adUrl("https://example.com/ad/" + i)
                    .startDate(startDate)
                    .endDate(endDate)
                    .adPlaytime(RANDOM.nextInt(30) + 1)  // 1 ~ 30초의 광고
                    .isUsed(false)
                    .build();
            ads.add(ad);
        }

        // Create Scheduled Ads (5%)
        for (int i = 0; i < scheduledAdsCount; i++) {
            LocalDate startDate = today.plusDays(RANDOM.nextInt(30));  // 최대 30일 후부터 시작
            LocalDate endDate = startDate.plusDays(RANDOM.nextInt(30));  // 시작일 이후 최대 30일까지 활성화
            Ad ad = Ad.builder()
                    .adUrl("https://example.com/ad/" + (activeAdsCount + i))
                    .startDate(startDate)
                    .endDate(endDate)
                    .adPlaytime(RANDOM.nextInt(30) + 1)  // 1 ~ 30초의 광고
                    .isUsed(false)
                    .build();
            ads.add(ad);
        }

        // Create Expired Ads (5%)
        for (int i = 0; i < expiredAdsCount; i++) {
            LocalDate endDate = today.minusDays(RANDOM.nextInt(30));  // 최대 30일 전에 만료
            LocalDate startDate = endDate.minusDays(RANDOM.nextInt(30));  // 최대 30일 전에 시작
            Ad ad = Ad.builder()
                    .adUrl("https://example.com/ad/" + (activeAdsCount + scheduledAdsCount + i))
                    .startDate(startDate)
                    .endDate(endDate)
                    .adPlaytime(RANDOM.nextInt(30) + 1)  // 1 ~ 30초의 광고
                    .isUsed(false)
                    .build();
            ads.add(ad);
        }

        // Save all ads to the repository
        adRepository.saveAll(ads);

        return String.format("Dummy ad data generation completed. Total Ads: %d, Active Ads: %d, Scheduled Ads: %d, Expired Ads: %d",
                TOTAL_ADS, activeAdsCount, scheduledAdsCount, expiredAdsCount);
    }
}
