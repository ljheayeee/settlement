package com.settlement.project.main.revenues.service;

import com.settlement.project.common.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.common.revenues.entity.Revenue;
import com.settlement.project.common.revenues.repository.RevenueRepository;
import com.settlement.project.common.util.DateRange;
import com.settlement.project.common.video.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RevenueService {


    private final RevenueRepository revenueRepository;

    private final VideoRepository videoRepository;

    public RevenueService(RevenueRepository revenueRepository,
                          VideoRepository videoRepository) {
        this.revenueRepository = revenueRepository;
        this.videoRepository = videoRepository;
    }

    @Transactional(readOnly = true)
    public UserRevenueDetailResponseDto getUserRevenueDetail(Long userId, String period) {
        DateRange dateRange = DateRange.of(period, LocalDate.now());
        List<Revenue> revenues = revenueRepository.findByUserIdAndCreatedAtBetween(
                userId,
                dateRange.getStart().atStartOfDay(),
                dateRange.getEnd().atTime(23, 59, 59)
        );

        Map<Long, String> videoTitleMap = videoRepository.findTitlesByVideoIds(
                revenues.stream().map(Revenue::getVideoId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(VideoRepository.VideoTitleProjection::getId, VideoRepository.VideoTitleProjection::getTitle));

        return UserRevenueDetailResponseDto.fromEntity(revenues, period, videoTitleMap);
    }


}
