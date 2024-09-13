package com.settlement.project.common.videoadstats.entity;

import com.settlement.project.common.base.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "video_ad_stats")
public class VideoAdStats extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "ad_id")
    private Long adId;

    @Column(name = "daily_ad_view")
    private Long dailyAdView;

    @Column(name = "total_ad_view")
    private Long totalAdView;

    public void incrementDailyAdView() {
        this.dailyAdView++;
    }

    @Builder
    public VideoAdStats(Long videoId, Long adId, Long totalAdView, Long dailyAdView) {
        this.videoId = videoId;
        this.adId = adId;
        this.dailyAdView = dailyAdView;
        this.totalAdView = totalAdView;
    }

    public static VideoAdStats createNewStats(Long videoId, Long adId) {
        return builder()
                .videoId(videoId)
                .adId(adId)
                .dailyAdView(0L)
                .totalAdView(0L)
                .build();
    }

    public void updateTotalAndResetDaily() {
        this.totalAdView += this.dailyAdView;
        this.dailyAdView = 0L;
    }

    public Long getDailyAdView() {
        return dailyAdView != null ? dailyAdView : 0L;
    }

    public Long getTotalAdView() {
        return totalAdView != null ? totalAdView : 0L;
    }

    public void updateTotalAdView() {
        this.totalAdView += this.dailyAdView;
        this.dailyAdView = 0L;
    }
}