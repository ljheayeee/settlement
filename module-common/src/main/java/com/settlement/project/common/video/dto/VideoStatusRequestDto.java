package com.settlement.project.common.video.dto;

import com.settlement.project.common.video.entity.VideoStatusEnum;
import lombok.Getter;

@Getter
public class VideoStatusRequestDto {
    private final VideoStatusEnum status;


    // Builder를 위한 모든 필드 생성자
    private VideoStatusRequestDto(VideoStatusEnum status) {
        this.status = status;
    }
}
