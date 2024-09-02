package com.settlement.project.revenues.dto;

import com.settlement.project.revenues.entity.Revenue;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserRevenueDetailResponseDto {
    private final String period;
    private final Long totalRevenue;
    private final Long totalVideoRevenue;
    private final Long totalAdRevenue;
    private final List<VideoRevenueDetail> videoRevenueDetails;

    public static UserRevenueDetailResponseDto fromEntity(List<Revenue> revenues, String period, Map<Long, String> videoTitles) {
        long totalRevenue = revenues.stream().mapToLong(Revenue::getRevenueTotal).sum();
        long totalVideoRevenue = revenues.stream().mapToLong(Revenue::getRevenueVideo).sum();
        long totalAdRevenue = revenues.stream().mapToLong(Revenue::getRevenueAd).sum();

        Map<Long, VideoRevenueDetail> videoRevenueMap = revenues.stream()
                .collect(Collectors.groupingBy(
                        Revenue::getVideoId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                revenueList -> VideoRevenueDetail.fromEntity(revenueList, videoTitles.get(revenueList.get(0).getVideoId()))
                        )
                ));

        return UserRevenueDetailResponseDto.builder()
                .period(period)
                .totalRevenue(totalRevenue)
                .totalVideoRevenue(totalVideoRevenue)
                .totalAdRevenue(totalAdRevenue)
                .videoRevenueDetails(new ArrayList<>(videoRevenueMap.values()))
                .build();
    }

    @Getter
    @Builder
    public static class VideoRevenueDetail {
        private final Long videoId;
        private final String videoTitle;
        private final Long videoRevenue;
        private final Long adRevenue;
        private final Long totalRevenue;

        public static VideoRevenueDetail fromEntity(List<Revenue> revenues, String videoTitle) {
            long videoRevenue = revenues.stream().mapToLong(Revenue::getRevenueVideo).sum();
            long adRevenue = revenues.stream().mapToLong(Revenue::getRevenueAd).sum();
            long totalRevenue = videoRevenue + adRevenue;

            return VideoRevenueDetail.builder()
                    .videoId(revenues.get(0).getVideoId())
                    .videoTitle(videoTitle)
                    .videoRevenue(videoRevenue)
                    .adRevenue(adRevenue)
                    .totalRevenue(totalRevenue)
                    .build();
        }
    }
}