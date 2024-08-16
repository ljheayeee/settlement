package com.settlement.video.service;

import com.settlement.video.entity.*;
import com.settlement.video.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import com.settlement.video.dto.VideoRequestDto;
import com.settlement.video.repository.AdRepository;
import com.settlement.video.repository.StatsRepository;
import com.settlement.video.repository.UserRepository;
import com.settlement.video.repository.VideoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class VideoServicePerformanceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private StatsRepository statsRepository;

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        videoService = new VideoService(videoRepository, userRepository, adRepository, statsRepository);
    }

    @Test
    void compareSelectAdsPerformance() {
        // Prepare test data
        int videoLength = 3600; // 60 minutes video in seconds
        int adCount = (videoLength / 60 - 1) / 5; // Number of ads needed

        User testUser = new User("testUser", "password", "test@example.com", UserRoleEnum.USER);
        testUser.setId(1L);

        // Create VideoRequestDto using Builder
        VideoRequestDto videoRequestDto = VideoRequestDto.builder()
                .userId(1L)
                .title("Test Video")
                .description("Test Description")
                .videoUrl("http://example.com/video")
                .playTime(videoLength)
                .status(VideoStatusEnum.ACTIVATE)
                .build();

        // Mock video creation
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video savedVideo = invocation.getArgument(0);
            return Video.builder()
                    .user(savedVideo.getUser())
                    .title(savedVideo.getTitle())
                    .description(savedVideo.getDescription())
                    .videoUrl(savedVideo.getVideoUrl())
                    .view(savedVideo.getView())
                    .status(savedVideo.getStatus())
                    .playTime(savedVideo.getPlayTime())
                    .build();
        });

        List<Ad> unusedAds = createTestAds(10000); // Create 10,000 unused ads
        List<Ad> allAds = new ArrayList<>(unusedAds);

        // Mock repository methods
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(adRepository.findByIsUsedFalse()).thenReturn(unusedAds);
        when(adRepository.findAll()).thenReturn(allAds);

        // Test original selectAds method
        long startTime = System.currentTimeMillis();
        videoService.createVideo(videoRequestDto);
        long endTime = System.currentTimeMillis();
        long originalMethodTime = endTime - startTime;

        System.out.println("Original selectAds method took " + originalMethodTime + " ms");

        // Reset mocks and data
        reset(adRepository, statsRepository);
        unusedAds = createTestAds(10000);
        allAds = new ArrayList<>(unusedAds);
        when(adRepository.findByIsUsedFalse()).thenReturn(unusedAds);
        when(adRepository.findAll()).thenReturn(allAds);

        // Test optimized selectAdsOptimized method
        // You'll need to modify VideoService to use selectAdsOptimized instead of selectAds
        startTime = System.currentTimeMillis();
        videoService.createVideo(videoRequestDto);
        endTime = System.currentTimeMillis();
        long optimizedMethodTime = endTime - startTime;

        System.out.println("Optimized selectAdsOptimized method took " + optimizedMethodTime + " ms");

        // Compare results
        System.out.println("Performance difference: " + (originalMethodTime - optimizedMethodTime) + " ms");
    }

    private List<Ad> createTestAds(int count) {
        List<Ad> ads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Ad ad = Ad.builder()
                    .adUrl("http://example.com/ad" + i)
                    .endDate(LocalDate.now().plusDays(30))
                    .isUsed(false)
                    .build();
            ads.add(ad);
        }
        return ads;
    }
}