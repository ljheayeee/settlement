package com.settlement.video.entity;

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
    @Column(name = "stats_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "ad_id")
    private Ad ad;

    @Column(name = "stats_ad_view")
    private Integer statsAdView;

    @Builder
    public Stats(Video video, Ad ad, Integer statsAdView) {
        this.video = video;
        this.ad = ad;
        this.statsAdView = statsAdView;
    }

    public static Stats create(Video video, Ad ad) {
        return new Stats(video, ad, 0);
    }

    public void incrementAdView() {
        this.statsAdView++;
    }
}
