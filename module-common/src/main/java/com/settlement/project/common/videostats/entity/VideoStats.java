package com.settlement.project.common.videostats.entity;

import com.settlement.project.common.base.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoStats extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_stats_id", nullable = false)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_views", nullable = false)
    private long dailyViews;  // 일일 조회수

    @Column(name = "daily_watch_time", nullable = false)
    private long dailyWatchTime;  // 일일 재생시간

    @Column(name = "total_views", nullable = false)
    private long totalViews;  // 누적 조회수

    @Column(name = "total_watch_time", nullable = false)
    private long totalWatchTime;  // 누적 재생시간

    @Builder
    public VideoStats(Long videoId, Long userId, long totalViews, long totalWatchTime, long dailyViews, long dailyWatchTime) {
        this.videoId = videoId;
        this.userId = userId;
        this.totalViews = totalViews;
        this.totalWatchTime = totalWatchTime;
        this.dailyViews = dailyViews;
        this.dailyWatchTime = dailyWatchTime;
    }

    /**
     * 통계 업데이트 - 누적 통계 및 일일 통계 계산
     */
    public void updateStats(long totalViews, long totalWatchTime, VideoStats previousDayStats) {
        long previousTotalViews = previousDayStats != null ? previousDayStats.getTotalViews() : 0;
        long previousTotalWatchTime = previousDayStats != null ? previousDayStats.getTotalWatchTime() : 0;

        this.totalViews = totalViews;
        this.totalWatchTime = totalWatchTime;

        // 일일 통계 계산
        this.dailyViews = this.totalViews - previousTotalViews;
        this.dailyWatchTime = this.totalWatchTime - previousTotalWatchTime;
    }
}
