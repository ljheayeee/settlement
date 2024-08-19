package com.settlement.project.stats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stats")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "ad_id")
    private Long adId;

    @Column(name = "stats_ad_view")
    private Integer statsAdView;


    public void incrementAdView() {
        this.statsAdView++;
    }

    @Builder
    public Stats(Long videoId, Long adId, Integer statsAdView) {
        this.videoId = videoId;
        this.adId = adId;
        this.statsAdView = statsAdView;
    }
    public static Stats createNewStats(Long videoId, Long adId) {
        return Stats.builder()
                .videoId(videoId)
                .adId(adId)
                .statsAdView(0)
                .build();
    }


}
