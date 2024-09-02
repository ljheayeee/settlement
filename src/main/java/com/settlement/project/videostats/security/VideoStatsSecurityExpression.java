package com.settlement.project.videostats.security;

import com.settlement.project.user.service.UserService;
import com.settlement.project.video.entity.Video;
import com.settlement.project.video.service.VideoService;
import org.springframework.security.core.Authentication;
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
