package com.settlement.project.video.entity;

import com.settlement.project.video.dto.VideoRequestDto;
import com.settlement.project.common.entity.Timestamped;
import com.settlement.project.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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


    public void update(VideoRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.description = requestDto.getDescription();
        this.videoUrl = requestDto.getVideoUrl();
        this.playTime = requestDto.getPlayTime();
        this.status = requestDto.getStatus();

    }

    public void updateStatus(VideoStatusEnum newStatus) {
        this.status = newStatus;
    }

    public String getUrl() {
        return this.videoUrl;
    }
}