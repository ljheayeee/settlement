package com.settlement.project.main.videostats.security;

import com.settlement.project.common.video.entity.Video;
import com.settlement.project.main.video.service.VideoService;
import org.springframework.stereotype.Component;

@Component
public class VideoStatsSecurityExpression {

    private final VideoService videoService;


    public VideoStatsSecurityExpression(VideoService videoService) {
        this.videoService = videoService;
    }

    public boolean isVideoOwner(Long videoId, Long userId) {
        Video video = videoService.getVideoById(videoId);
        return video.getUser().getId().equals(userId);
    }
}
