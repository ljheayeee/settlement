package com.settlement.project.stats.repository;

import com.settlement.project.ads.entity.Ad;
import com.settlement.project.stats.entity.Stats;
import com.settlement.project.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {


    List<Stats> findByVideoId(Long videoId);
    Optional<Stats> findByVideoIdAndAdId(Long videoId, Long adId);


}
