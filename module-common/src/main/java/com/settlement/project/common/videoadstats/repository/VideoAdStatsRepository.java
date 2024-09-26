package com.settlement.project.common.videoadstats.repository;

import com.settlement.project.common.videoadstats.entity.VideoAdStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VideoAdStatsRepository extends JpaRepository<VideoAdStats, Long> {


    List<VideoAdStats> findByVideoId(Long videoId);
    Optional<VideoAdStats> findByVideoIdAndAdId(Long videoId, Long adId);

    List<VideoAdStats> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT v FROM VideoAdStats v WHERE v.videoId = :videoId AND DATE(v.createdAt) = :date")
    List<VideoAdStats> findByVideoIdAndDate(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT v FROM VideoAdStats v WHERE v.id = :id AND DATE(v.createdAt) = :date")
    Optional<VideoAdStats> findByIdAndDate(@Param("id") Long id, @Param("date") LocalDate date);


    interface VideoTitleProjection {
        Long getId();
        String getTitle();
    }

    @Query(value = "SELECT * FROM video_ad_stats WHERE video_id = :videoId AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<VideoAdStats> findByVideoIdAndCreatedAtBetween(
            @Param("videoId") Long videoId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
