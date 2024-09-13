package com.settlement.project.main.watchhisotry.dto;

import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.watchhistory.entity.WatchHistory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WatchHistoryRequestDto {
    private Long videoId;
    private Integer watchHistoryTime;

    public WatchHistory toEntity(Video video) {
        return WatchHistory.builder()
                .video(video)
                .watchHistoryTime(watchHistoryTime)
                .build();
    }
}
