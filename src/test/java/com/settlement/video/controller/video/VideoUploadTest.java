package com.settlement.video.controller.video;

import com.settlement.video.dto.VideoRequestDto;
import com.settlement.video.dto.VideoResponseDto;
import com.settlement.video.entity.User;
import com.settlement.video.entity.UserRoleEnum;
import com.settlement.video.entity.Video;
import com.settlement.video.entity.VideoStatusEnum;
import com.settlement.video.repository.UserRepository;
import com.settlement.video.repository.VideoRepository;
import com.settlement.video.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoUploadTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    private VideoRequestDto videoRequestDto;
    private User user;
    private Video video;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "password123", "test@example.com", UserRoleEnum.USER);
        user.setId(1L);

        videoRequestDto = VideoRequestDto.builder()
                .userId(1L)
                .title("Test Video")
                .description("This is a test video")
                .videoUrl("http://example.com/video")
                .playTime(120)
                .status(VideoStatusEnum.ACTIVATE)
                .build();

        video = Video.builder()
                .user(user)
                .title("Test Video")
                .description("This is a test video")
                .videoUrl("http://example.com/video")
                .view(0)
                .playTime(120)
                .status(VideoStatusEnum.ACTIVATE)
                .build();
    }

    @Test
    void createVideo_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        // Act
        VideoResponseDto result = videoService.createVideo(videoRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Video", result.getTitle());
        assertEquals("This is a test video", result.getDescription());
        assertEquals("http://example.com/video", result.getVideoUrl());
        assertEquals(120, result.getPlayTime());
        assertEquals(VideoStatusEnum.ACTIVATE, result.getStatus());
        assertEquals(1L, result.getUserId());

        verify(userRepository, times(1)).findById(1L);
        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    void createVideo_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> videoService.createVideo(videoRequestDto));

        verify(userRepository, times(1)).findById(1L);
        verify(videoRepository, never()).save(any(Video.class));
    }
}