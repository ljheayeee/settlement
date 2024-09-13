package com.settlement.project.main.watchhisotry.dto;

import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.watchhistory.entity.WatchHistory;
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
