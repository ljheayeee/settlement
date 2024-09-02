package com.settlement.project.videostats.dto;

import lombok.Getter;

@Getter
public class VideoStatsInfoDto {
    private final Long id;
    private final Long userId;
    private final Long videoId;
    private final Long views;
    private final Integer videoDuration;

    public VideoStatsInfoDto(Long id, Long userId, Long videoId, Long views, Integer videoDuration) {
        this.id = id;
        this.userId = userId;
        this.videoId = videoId;
        this.views = views;
        this.videoDuration = videoDuration;
    }
}