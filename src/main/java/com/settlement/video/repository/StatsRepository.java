package com.settlement.video.repository;

import com.settlement.video.entity.Stats;
import com.settlement.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    //test
    long countByVideoId(Long videoId);  // 이 메서드 추가
}
