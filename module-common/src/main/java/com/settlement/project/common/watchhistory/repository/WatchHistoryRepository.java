package com.settlement.project.common.watchhistory.repository;

import com.settlement.project.common.user.entity.User;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.watchhistory.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    Optional<WatchHistory> findByUserAndVideo(User user, Video video);

    @Query("SELECT COUNT(w) FROM WatchHistory w WHERE w.video.id = :videoId AND CAST(w.createdAt AS DATE) = :date")
    int countViewsByVideoAndDate(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(w.watchHistoryTime), 0) FROM WatchHistory w WHERE w.video.id = :videoId AND CAST(w.createdAt AS DATE) = :date")
    long sumWatchTimeByVideoAndDate(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(w) FROM WatchHistory w WHERE w.video.id = :videoId AND CAST(w.createdAt AS DATE) BETWEEN :startDate AND :endDate")
    int countViewsByVideoAndDateRange(@Param("videoId") Long videoId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(w.watchHistoryTime), 0) FROM WatchHistory w WHERE w.video.id = :videoId AND CAST(w.createdAt AS DATE) BETWEEN :startDate AND :endDate")
    long sumWatchTimeByVideoAndDateRange(@Param("videoId") Long videoId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT DISTINCT wh.video.id FROM WatchHistory wh WHERE CAST(wh.createdAt AS date) = :date")
    List<Long> findActiveVideoIdsByDate(@Param("date") LocalDate date);


    @Query("SELECT COUNT(wh) FROM WatchHistory wh WHERE wh.video.id = :videoId AND CAST(wh.createdAt AS date) <= :date")
    long countTotalViewsByVideoAndDateBefore(@Param("videoId") Long videoId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(wh.watchHistoryTime), 0) FROM WatchHistory wh WHERE wh.video.id = :videoId AND CAST(wh.createdAt AS date) <= :date")
    long sumTotalWatchTimeByVideoAndDateBefore(@Param("videoId") Long videoId, @Param("date") LocalDate date);

}
