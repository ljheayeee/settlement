package com.settlement.project.batch.videoadstats;

import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import com.settlement.project.common.videoadstats.repository.VideoAdStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoAdStatsBatchService {
    private final VideoAdStatsRepository videoAdStatsRepository;

    public VideoAdStatsBatchService(VideoAdStatsRepository videoAdStatsRepository) {
        this.videoAdStatsRepository = videoAdStatsRepository;
    }

    @Transactional
    public void updateTotalAndResetDailyViews() {
        List<VideoAdStats> allStats = videoAdStatsRepository.findAll();
        for (VideoAdStats stat : allStats) {
            stat.updateTotalAndResetDaily();
            videoAdStatsRepository.save(stat);
        }
    }
}
