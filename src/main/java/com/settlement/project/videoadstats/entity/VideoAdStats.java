package com.settlement.project.videoadstats.entity;

import com.settlement.project.common.entity.Timestamped;
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

    @Column(name = "stats_ad_view")
    private Long statsAdView;


    public void incrementAdView() {
        this.statsAdView++;
    }

    @Builder
    public VideoAdStats(Long videoId, Long adId, Long statsAdView) {
        this.videoId = videoId;
        this.adId = adId;
        this.statsAdView = statsAdView;
    }
    public static VideoAdStats createNewStats(Long videoId, Long adId) {
        return VideoAdStats.builder()
                .videoId(videoId)
                .adId(adId)
                .statsAdView(0L)
                .build();
    }
    public void updateStatsAdView(Long statsAdView) {
        this.statsAdView = statsAdView;
    }

    public Long getStatsAdView() {
        return statsAdView != 0 ? statsAdView: 0;
    }

}
