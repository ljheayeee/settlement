package com.settlement.video.dto;

import com.settlement.video.entity.Video;
import com.settlement.video.entity.VideoStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VideoResponseDto {
    private  Long id;
    private  String title;
    private  String description;
    private  String videoUrl;
    private  Integer view;
    private  Integer playTime;
    private  VideoStatusEnum status;
    private  Long userId;

    public static VideoResponseDto fromEntity(Video video) {
        return VideoResponseDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .view(video.getView())
                .playTime(video.getPlayTime())
                .status(video.getStatus())
                .userId(video.getUser().getId())
                .build();
    }
}
