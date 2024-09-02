package com.settlement.project.video.dto;

import com.settlement.project.user.entity.User;
import com.settlement.project.video.entity.Video;
import com.settlement.project.video.entity.VideoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequestDto {
    private String title;
    private String description;
    private String videoUrl;
    private Integer playTime;
    private VideoStatusEnum status;

    // userId는 더 이상 클라이언트로부터 받지 않습니다.
    // 하지만 서비스 계층에서 사용하기 위해 유지할 수 있습니다.
    @Builder.Default
    private Long userId = null;

    // toEntity 메소드도 수정이 필요할 수 있습니다.
    public Video toEntity(User user) {
        return Video.builder()
                .user(user)
                .title(title)
                .description(description)
                .videoUrl(videoUrl)
                .view(0)
                .playTime(playTime != null ? playTime : 0)
                .status(VideoStatusEnum.ACTIVATE)
                .build();
    }
}