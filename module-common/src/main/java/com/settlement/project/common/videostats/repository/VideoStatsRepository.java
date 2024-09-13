package com.settlement.project.common.videostats.repository;

import com.settlement.project.common.videostats.entity.VideoStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoStatsRepository extends JpaRepository<VideoStats, Long> {

    @Query("SELECT vs FROM VideoStats vs WHERE vs.videoId = :videoId AND DATE(vs.createdAt) = :date")
    Optional<VideoStats> findByVideoIdAndCreatedAt(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT v.id FROM Video v WHERE v.status = 'ACTIVATE'")
    List<Long> findAllActiveVideoIds();

    @Query(value = "SELECT vs.* FROM video_stats vs " +
            "JOIN videos v ON vs.video_id = v.video_id " +
            "LEFT JOIN LATERAL (" +
            "    SELECT vs2.total_views FROM video_stats vs2 " +
            "    WHERE vs2.video_id = vs.video_id AND vs2.created_at < CAST(:startDate AS date) " +
            "    ORDER BY vs2.created_at DESC LIMIT 1" +
            ") AS prev_vs ON true " +
            "WHERE vs.user_id = :userId AND v.status = 'ACTIVATE' " +
            "AND vs.modified_at >= CAST(:startDate AS date) AND vs.modified_at < CAST(:endDate AS date) + INTERVAL '1 day' " +
            "ORDER BY COALESCE(vs.total_views - prev_vs.total_views, vs.total_views) DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<VideoStats> findTop5ByViewsInDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT vs.* FROM video_stats vs " +
            "JOIN videos v ON vs.video_id = v.video_id " +
            "LEFT JOIN LATERAL (" +
            "    SELECT vs2.total_views * v2.play_time AS prev_watch_time " +
            "    FROM video_stats vs2 " +
            "    JOIN videos v2 ON vs2.video_id = v2.video_id " +
            "    WHERE vs2.video_id = vs.video_id AND vs2.created_at < CAST(:startDate AS date) " +
            "    ORDER BY vs2.created_at DESC LIMIT 1" +
            ") AS prev_vs ON true " +
            "WHERE vs.user_id = :userId AND v.status = 'ACTIVATE' " +
            "AND vs.modified_at >= CAST(:startDate AS date) AND vs.modified_at < CAST(:endDate AS date) + INTERVAL '1 day' " +
            "ORDER BY COALESCE((vs.total_views * v.play_time) - prev_vs.prev_watch_time, vs.total_views * v.play_time) DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<VideoStats> findTop5ByWatchTimeInDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);


    @Query("SELECT vs FROM VideoStats vs WHERE vs.videoId = :videoId AND DATE(vs.createdAt) = :date ORDER BY vs.createdAt DESC LIMIT 1")
    Optional<VideoStats> findLatestStatsByVideoIdAndDate(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT vs.videoId FROM VideoStats vs JOIN Video v ON vs.videoId = v.id WHERE DATE(vs.createdAt) = :date AND v.status = 'ACTIVATE'")
    List<Long> findActiveVideoIdsByDate(@Param("date") LocalDate date);

    @Query("SELECT vs FROM VideoStats vs JOIN Video v ON vs.videoId = v.id " +
            "WHERE DATE(vs.createdAt) = :date AND v.status = 'ACTIVATE'")
    List<VideoStats> findByCreatedDateForActiveVideos(@Param("date") LocalDate date);

    @Query("SELECT vs FROM VideoStats vs WHERE DATE(vs.createdAt) = :date")
    List<VideoStats> findByCreatedDate(@Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE VideoStats v SET v.createdAt = :createdAt, v.modifiedAt = :modifiedAt WHERE v.id = :id")
    void updateCreatedAtAndModifiedAt(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt, @Param("modifiedAt") LocalDateTime modifiedAt);
}