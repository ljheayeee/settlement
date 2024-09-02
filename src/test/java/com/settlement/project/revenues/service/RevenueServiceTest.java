package com.settlement.project.revenues.service;

import com.settlement.project.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.revenues.entity.Revenue;
import com.settlement.project.revenues.repository.RevenueRepository;
import com.settlement.project.revenues.service.RevenueService;
import com.settlement.project.user.entity.User;
import com.settlement.project.video.entity.Video;
import com.settlement.project.video.service.VideoService;
import com.settlement.project.videoadstats.entity.VideoAdStats;
import com.settlement.project.videoadstats.service.VideoAdStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RevenueServiceTest {

    @InjectMocks
    private RevenueService revenueService;

    @Mock
    private RevenueRepository revenueRepository;

    @Mock
    private VideoService videoService;

    @Mock
    private VideoAdStatsService videoAdStatsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("일일 정산 계산 테스트")
    void testCalculateDailyRevenue() {
        // Arrange
        LocalDate calculationDate = LocalDate.now();

        User user = new User();
        user.setId(1L);



        Video video = Video.builder()
                .title("테스트 비디오")
                .view(550000)
                .user(user)  // User 객체 설정
                .build();

        when(videoService.getAllVideos()).thenReturn(Arrays.asList(video));
        when(revenueRepository.findFirstByVideoIdAndCreatedAtBeforeOrderByCreatedAtDesc(anyLong(), any())).thenReturn(null);
        when(videoAdStatsService.getVideoAdStats(anyLong())).thenReturn(Arrays.asList(VideoAdStats.builder().build()));

        // videoId를 설정하기 위한 추가 작업
        // 실제 구현에 따라 이 부분은 달라질 수 있습니다.
        when(videoService.getVideoById(anyLong())).thenReturn(video);

        // Act
        revenueService.calculateDailyRevenue(RevenueCalculationRequestDto.builder().calculationDate(calculationDate).build());

        // Assert
        verify(revenueRepository, times(1)).save(any(Revenue.class));
    }

    @Test
    @DisplayName("사용자 수익 상세 정보 조회 테스트")
    void testGetUserRevenueDetail() {
        // 이 테스트는 특정 사용자의 수익 상세 정보를 올바르게 조회하는지 확인합니다.

        // Arrange
        Long userId = 1L;
        String period = "daily";
        LocalDate date = LocalDate.now();
        Revenue revenue = Revenue.builder()
                .userId(userId)
                .videoId(1L)
                .revenueVideo(60500L)
                .revenueAd(65500L)
                .build();
        when(revenueRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(), any())).thenReturn(Arrays.asList(revenue));
        when(videoService.getVideoTitles(anySet())).thenReturn(Map.of(1L, "테스트 비디오"));

        // Act
        UserRevenueDetailResponseDto result = revenueService.getUserRevenueDetail(userId, period);

        // Assert
        assertNotNull(result);
        assertEquals(126000L, result.getTotalRevenue());
        assertEquals(60500L, result.getTotalVideoRevenue());
        assertEquals(65500L, result.getTotalAdRevenue());
        assertEquals(1, result.getVideoRevenueDetails().size());
    }
}