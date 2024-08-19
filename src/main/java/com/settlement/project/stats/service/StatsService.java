package com.settlement.project.stats.service;

import com.settlement.project.ads.entity.Ad;
import com.settlement.project.stats.entity.Stats;
import com.settlement.project.video.entity.Video;
import com.settlement.project.stats.repository.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }



    @Transactional
    public void createStatsForVideoAds(Long videoId, List<Long> adIds) {
        List<Stats> stats = adIds.stream()
                .map(adId -> Stats.createNewStats(videoId, adId))
                .collect(Collectors.toList());

        statsRepository.saveAll(stats);
    }

    public List<Long> getAdIdsForVideo(Long videoId) {
        return statsRepository.findByVideoId(videoId).stream()
                .map(Stats::getAdId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementAdViewCount(Long videoId, Long adId) {
        Stats stats = statsRepository.findByVideoIdAndAdId(videoId, adId)
                        .orElseThrow(() -> new RuntimeException("Stats not found for video and ad"));
        stats.incrementAdView();
        statsRepository.save(stats);
    }


    public List<Stats> getStatsForVideo(Long videoId) {
        return statsRepository.findByVideoId(videoId);
    }

}