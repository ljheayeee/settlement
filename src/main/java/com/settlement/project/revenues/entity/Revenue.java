package com.settlement.project.revenues.entity;

import com.settlement.project.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revenues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Revenue extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "revenue_video", nullable = false)
    private Long revenueVideo;

    @Column(name = "revenue_ad", nullable = false)
    private Long revenueAd;

    @Column(name = "revenue_total", nullable = false)
    private Long revenueTotal;

    @Builder
    public Revenue(Long userId, Long videoId, Long revenueVideo, Long revenueAd,Long revenueTotal) {
        this.userId = userId;
        this.videoId = videoId;
        this.revenueVideo = revenueVideo;
        this.revenueAd = revenueAd;
        this.revenueTotal = revenueTotal;
    }

    public void updateRevenue(Long revenueVideo, Long revenueAd) {
        this.revenueVideo = revenueVideo;
        this.revenueAd = revenueAd;
        this.revenueTotal = revenueVideo + revenueAd;
    }
}
