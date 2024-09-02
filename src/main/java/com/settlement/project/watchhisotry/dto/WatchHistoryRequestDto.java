package com.settlement.project.watchhisotry.dto;

import com.settlement.project.user.entity.User;
import com.settlement.project.video.entity.Video;
import com.settlement.project.watchhisotry.entity.WatchHistory;
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
