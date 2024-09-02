package com.settlement.project.watchhisotry.dto;

import com.settlement.project.video.entity.Video;
import com.settlement.project.watchhisotry.entity.WatchHistory;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StartWatchingRequestDto {
    private Long videoId;

    @Builder
    public WatchHistory toEntity(Video video) {
        return WatchHistory.builder()
                .video(video)
                .build();
    }


}
