package com.settlement.project.revenues.repository;

import com.settlement.project.revenues.dto.DailyRevenueResponseDto;
import com.settlement.project.revenues.entity.Revenue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    List<Revenue> findByModifiedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Revenue> findByUserId(Long userId);

    List<Revenue> findByVideoId(Long videoId);

    @Query("SELECT r FROM Revenue r WHERE r.modifiedAt BETWEEN :start AND :end " +
            "GROUP BY r.videoId ORDER BY SUM(r.revenueTotal) DESC")
    List<Revenue> findTopRevenueVideos(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);

    @Query("SELECT SUM(r.revenueTotal) FROM Revenue r WHERE DATE(r.modifiedAt) = DATE(:date)")
    Long calculateTotalRevenueForDate(@Param("date") LocalDateTime date);

    @Query("SELECT NEW com.settlement.project.revenues.dto.DailyRevenueResponseDto(DATE(r.modifiedAt), SUM(r.revenueTotal)) " +
            "FROM Revenue r WHERE r.modifiedAt BETWEEN :start AND :end " +
            "GROUP BY DATE(r.modifiedAt) ORDER BY DATE(r.modifiedAt)")
    List<DailyRevenueResponseDto> calculateDailyTotalRevenueBetween(@Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end);

    @Query("SELECT SUM(r.revenueTotal) FROM Revenue r " +
            "WHERE r.userId = :userId AND r.modifiedAt BETWEEN :start AND :end")
    Long calculateTotalRevenueForUserBetween(@Param("userId") Long userId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Revenue r WHERE r.videoId = :videoId AND DATE(r.modifiedAt) = :date")
    Optional<Revenue> findByVideoIdAndDate(@Param("videoId") Long videoId, @Param("date") LocalDate date);


    List<Revenue> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    Optional<Revenue> findFirstByVideoIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long videoId, LocalDateTime dateTime);

    List<Revenue> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}