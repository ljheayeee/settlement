package com.settlement.video.repository;

import com.settlement.video.entity.Stats;
import com.settlement.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<Stats, Long> {

}
