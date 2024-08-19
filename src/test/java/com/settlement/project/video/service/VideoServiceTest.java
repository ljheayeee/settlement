package com.settlement.project.video.service;

import com.settlement.project.ads.service.AdService;
import com.settlement.project.stats.service.StatsService;
import com.settlement.project.user.entity.User;
import com.settlement.project.user.service.UserService;
import com.settlement.project.video.dto.VideoRequestDto;
import com.settlement.project.video.dto.VideoResponseDto;
import com.settlement.project.video.entity.Video;
import com.settlement.project.video.entity.VideoStatusEnum;
import com.settlement.project.video.exception.AdPlaybackException;
import com.settlement.project.video.exception.VideoCreationException;
import com.settlement.project.video.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private UserService userService;
    @Mock
    private AdService adService;
    @Mock
    private StatsService statsService;

    @InjectMocks
    private VideoService videoService;

    private User user;
    private Video video;
    private VideoRequestDto videoRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        video = Video.builder()
                .user(user)
                .title("Test Video")
                .description("Test Description")
                .videoUrl("http://test.com/video")
                .view(0)
                .status(VideoStatusEnum.ACTIVATE)
                .playTime(600)
                .build();

        // id를 수동으로 설정
        ReflectionTestUtils.setField(video, "id", 1L);

        videoRequestDto = VideoRequestDto.builder()
                .userId(1L)
                .title("Test Video")
                .description("Test Description")
                .videoUrl("http://test.com/video")
                .playTime(600)
                .build();
    }

    @Test
    void createVideo_Success() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        VideoResponseDto result = videoService.createVideo(videoRequestDto);

        assertNotNull(result);
        assertEquals("Test Video", result.getTitle());
        verify(adService).assignAdsToVideo(1L, 10);
    }

    @Test
    void createVideo_Failure() {
        when(userService.findUserById(1L)).thenThrow(new RuntimeException("User not found"));

        assertThrows(VideoCreationException.class, () -> videoService.createVideo(videoRequestDto));
    }

    @Test
    void updateVideoStatus_Success() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        VideoResponseDto result = videoService.updateVideoStatus(1L, VideoStatusEnum.INACTIVATE, 1L);

        assertNotNull(result);
        assertEquals(VideoStatusEnum.INACTIVATE, result.getStatus());
    }

    @Test
    void updateVideoStatus_AccessDenied() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        assertThrows(AccessDeniedException.class, () -> videoService.updateVideoStatus(1L, VideoStatusEnum.INACTIVATE, 2L));
    }

    @Test
    void getAllActiveVideos_Success() {
        List<Video> videos = Arrays.asList(video);
        Page<Video> videoPage = new PageImpl<>(videos);
        Pageable pageable = PageRequest.of(0, 10); // 페이지 번호 0, 페이지 크기 10으로 설정

        when(videoRepository.findByStatus(eq(VideoStatusEnum.ACTIVATE), eq(pageable))).thenReturn(videoPage);

        Page<VideoResponseDto> result = videoService.getAllActiveVideos(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getVideoById_Success() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        Video result = videoService.getVideoById(1L);

        assertNotNull(result);
        assertEquals("Test Video", result.getTitle());
    }

    @Test
    void getVideoById_NotFound() {
        when(videoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> videoService.getVideoById(1L));
    }

    @Test
    void checkAndPlayAd_Success() {
        List<Long> adIds = Arrays.asList(1L, 2L, 3L);
        when(statsService.getAdIdsForVideo(1L)).thenReturn(adIds);

        videoService.checkAndPlayAd(1L, 300);

        verify(statsService).incrementAdViewCount(1L, 1L);
    }

    @Test
    void checkAndPlayAd_Failure() {
        when(statsService.getAdIdsForVideo(1L)).thenThrow(new RuntimeException("Stats service error"));

        assertThrows(AdPlaybackException.class, () -> videoService.checkAndPlayAd(1L, 300));
    }
}