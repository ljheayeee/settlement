package com.settlement.project.video.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamingResponseDto {
    private final String url;
    private final boolean isAd;
    private final long nextCheckpoint;

    @Override
    public String toString() {
        return "StreamingResponseDto{" +
                "url='" + url + '\'' +
                ", isAd=" + isAd +
                ", nextCheckpoint=" + nextCheckpoint +
                '}';
    }
}
