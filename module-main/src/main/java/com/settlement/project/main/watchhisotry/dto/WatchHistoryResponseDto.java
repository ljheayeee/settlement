package com.settlement.project.main.watchhisotry.dto;

import com.settlement.project.common.watchhistory.entity.WatchHistory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WatchHistoryResponseDto {
    private Long id;
    private Long userId;
    private Long videoId;
    private Integer watchHistoryTime;

    public static WatchHistoryResponseDto fromEntity(WatchHistory watchHistory) {
        return builder()
                .id(watchHistory.getId())
                .userId(watchHistory.getUser().getId())
                .videoId(watchHistory.getVideo().getId())
                .watchHistoryTime(watchHistory.getWatchHistoryTime())
                .build();
    }
}
