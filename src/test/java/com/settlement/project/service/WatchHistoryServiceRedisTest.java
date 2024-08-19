//package com.settlement.video.service;
//
//import com.settlement.video.entity.*;
//import com.settlement.video.repository.WatchHistoryRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.time.Clock;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//public class WatchHistoryServiceRedisTest {
//
//    @Mock private WatchHistoryRepository watchHistoryRepository;
//    @Mock private UserService userService;
//    @Mock private VideoService videoService;
//    @Mock private RedisTemplate<String, String> redisTemplate;
//    @Mock private ValueOperations<String, String> valueOperations;
//
//    private WatchHistoryService watchHistoryService;
//    private Clock fixedClock;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//
//        fixedClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.systemDefault());
//
//        watchHistoryService = new WatchHistoryService(
//                watchHistoryRepository,
//                userService,
//                videoService,
//                redisTemplate,
//                fixedClock
//        );
//    }
//
//    @Test
//    void testPeriodicSaveToRedis() {
//        Long userId = 1L;
//        Long videoId = 1L;
//        User user = new User("testUser", "password", "test@email.com", UserRoleEnum.USER);
//        user.setId(userId);
//        Video video = Video.createVideo(user, "Test Video", "Description", "video_url", 600);
//        video.setId(videoId);
//
//        when(userService.findUserById(userId)).thenReturn(user);
//        when(videoService.getVideoById(videoId)).thenReturn(video);
//
//        for (int i = 1; i <= 20; i++) {
//            watchHistoryService.updateWatchHistoryTime(userId, videoId, i);
//        }
//
//        verify(valueOperations, times(20)).set(
//                eq("watch:" + userId + ":" + videoId),
//                anyString(),
//                eq(24L),
//                eq(TimeUnit.HOURS)
//        );
//
//        verify(videoService, times(20)).checkAndPlayAd(eq(userId), eq(videoId), anyInt());
//    }
//
//    @Test
//    void testStartWatching() {
//        Long userId = 1L;
//        Long videoId = 1L;
//        User user = new User("testUser", "password", "test@email.com", UserRoleEnum.USER);
//        user.setId(userId);
//        Video video = Video.createVideo(user, "Test Video", "Description", "video_url", 600);
//        video.setId(videoId);
//
//        when(userService.findUserById(userId)).thenReturn(user);
//        when(videoService.getVideoById(videoId)).thenReturn(video);
//        when(valueOperations.get("watch:" + userId + ":" + videoId)).thenReturn(null);
//        when(watchHistoryRepository.findByUserAndVideo(user, video)).thenReturn(Optional.empty());
//
//        watchHistoryService.startWatching(userId, videoId);
//
//        verify(watchHistoryRepository).save(argThat(watchHistory ->
//                watchHistory.getUser().equals(user) &&
//                        watchHistory.getVideo().equals(video) &&
//                        watchHistory.getWatchHistoryDate().equals(LocalDate.now(fixedClock)) &&
//                        watchHistory.getWatchHistoryTime() == 0
//        ));
//        verify(valueOperations).set(eq("watch:" + userId + ":" + videoId), eq("0"), eq(24L), eq(TimeUnit.HOURS));
//    }
//
//    @Test
//    void testPauseWatching() {
//        Long userId = 1L;
//        Long videoId = 1L;
//        User user = new User("testUser", "password", "test@email.com", UserRoleEnum.USER);
//        user.setId(userId);
//        Video video = Video.createVideo(user, "Test Video", "Description", "video_url", 600);
//        video.setId(videoId);
//
//        WatchHistory existingWatchHistory = new WatchHistory(user, video, LocalDate.now(fixedClock), 0);
//
//        when(userService.findUserById(userId)).thenReturn(user);
//        when(videoService.getVideoById(videoId)).thenReturn(video);
//        when(valueOperations.get("watch:" + userId + ":" + videoId)).thenReturn("30");
//        when(watchHistoryRepository.findByUserAndVideo(user, video)).thenReturn(Optional.of(existingWatchHistory));
//
//        watchHistoryService.pauseWatching(userId, videoId);
//
//        verify(watchHistoryRepository).save(argThat(watchHistory ->
//                watchHistory.getUser().equals(user) &&
//                        watchHistory.getVideo().equals(video) &&
//                        watchHistory.getWatchHistoryTime() == 30
//        ));
//    }
//}