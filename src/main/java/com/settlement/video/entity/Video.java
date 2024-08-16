package com.settlement.video.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "videos")
public class Video extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "view", nullable = false)
    private Integer view;

    @Column(name = "play_time")
    private Integer playTime; // 초로 계산

    @Enumerated(EnumType.STRING)
    private VideoStatusEnum status;


    @Builder
    private Video(User user, String title, String description, String videoUrl, Integer view, VideoStatusEnum status, Integer playTime) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.view = view;
        this.status = status;
        this.playTime = playTime;
    }

    public static Video createVideo(User user, String title, String description, String videoUrl, Integer playTime) {
        return new Video(user, title, description, videoUrl, 0, VideoStatusEnum.ACTIVATE, playTime);
    }

    public int getPlayTimeInMinutes() {
        return playTime / 60;
    }
}