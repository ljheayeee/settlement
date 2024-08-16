package com.settlement.video.dto;

import com.settlement.video.entity.Video;
import com.settlement.video.entity.VideoStatusEnum;
import lombok.Builder;
import lombok.Getter;
import com.settlement.video.entity.User;
@Getter
@Builder
public class VideoRequestDto {
    private Long userId;
    private String title;
    private String description;
    private String videoUrl;
    private Integer playTime;
    private VideoStatusEnum status;


    public Video toEntity(User user) {
        return Video.builder()
                .user(user)
                .title(title)
                .description(description)
                .videoUrl(videoUrl)
                .view(0)  // 초기 조회수는 0으로 설정
                .playTime(playTime != null ? playTime : 0)
                .status(status != null ? status : VideoStatusEnum.ACTIVATE)
                .build();
    }


}
