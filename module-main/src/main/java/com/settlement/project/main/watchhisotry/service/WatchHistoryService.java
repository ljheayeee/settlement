package com.settlement.project.main.watchhisotry.service;

import com.settlement.project.main.videoadstats.dto.PlayAdResponseDto;
import com.settlement.project.main.ads.service.AdService;
import com.settlement.project.common.user.entity.User;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.main.videoadstats.service.VideoAdStatsService;
import com.settlement.project.common.watchhistory.entity.WatchHistory;
import com.settlement.project.common.watchhistory.repository.WatchHistoryRepository;
import com.settlement.project.main.user.service.UserService;
import com.settlement.project.main.video.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class WatchHistoryService {
    private static final Logger log = LoggerFactory.getLogger(WatchHistoryService.class);


    private final WatchHistoryRepository watchHistoryRepository;
    private final UserService userService;
    private final VideoService videoService;
    private final RedisTemplate<String, String> redisTemplate;
    private final VideoAdStatsService videoAdStatsService;
    private final AdService adService;

    public WatchHistoryService(WatchHistoryRepository watchHistoryRepository,
                               UserService userService,
                               VideoService videoService,
                               RedisTemplate<String, String> redisTemplate, VideoAdStatsService videoAdStatsService, AdService adService) {
        this.watchHistoryRepository = watchHistoryRepository;
        this.userService = userService;
        this.videoService = videoService;
        this.redisTemplate = redisTemplate;
        this.videoAdStatsService = videoAdStatsService;
        this.adService = adService;
    }



    @Transactional
    public int startWatching(Long videoId,Long userId) {
        log.info("Starting watch session for user: {} and video: {}", userId, videoId);
        User user = userService.findUserById(userId);
        Video video = videoService.getVideoById(videoId);
        String key = String.format("watch:%d:%d", userId, videoId);

        String lastWatchHistoryTime = redisTemplate.opsForValue().get(key);
        int startWatchHistoryTime;

        if (lastWatchHistoryTime != null) {
            startWatchHistoryTime = Integer.parseInt(lastWatchHistoryTime);
            log.debug("Retrieved last watch time from Redis: {} for user: {} and video: {}", startWatchHistoryTime, userId, videoId);
        } else {
            WatchHistory watchHistory = watchHistoryRepository.findByUserAndVideo(user, video)
                    .orElse(null);

            if (watchHistory != null) {
                startWatchHistoryTime = watchHistory.getWatchHistoryTime();
                log.debug("Retrieved last watch time from DB: {} for user: {} and video: {}", startWatchHistoryTime, userId, videoId);
            } else {
                startWatchHistoryTime = 0;
                watchHistory = WatchHistory.builder()
                        .user(user)
                        .video(video)
                        .watchHistoryTime(startWatchHistoryTime)
                        .build();
                watchHistoryRepository.save(watchHistory);
                log.info("Created new watch history record for user: {} and video: {}", userId, videoId);
            }

            // 조회수 증가
            videoService.incrementViewCount(videoId);
        }

        redisTemplate.opsForValue().set(key, String.valueOf(startWatchHistoryTime), 48, TimeUnit.HOURS);
        log.info("Watch session started for user: {} and video: {} at time: {}", userId, videoId, startWatchHistoryTime);

        return startWatchHistoryTime;
    }

    @Transactional
    public PlayAdResponseDto updateWatchHistoryTime(Long userId, Long videoId, int watchHistoryTime) {
        String key = String.format("watch:%d:%d", userId, videoId);
        redisTemplate.opsForValue().set(key, String.valueOf(watchHistoryTime), 48, TimeUnit.HOURS);
        log.debug("Updated watch history time in Redis for user: {} and video: {} to time: {}", userId, videoId, watchHistoryTime);

        // VideoAdStatsService를 통해 광고 재생 정보 확인 및 처리
        PlayAdResponseDto adResponse = videoAdStatsService.checkAndPlayAd(videoId, watchHistoryTime);
        return adResponse;  // 광고가 재생된 경우 광고 정보를 반환, 없으면 null 반환
    }


    @Transactional
    public void pauseWatching(Long userId, Long videoId) {
        log.info("Pausing watch session for user: {} and video: {}", userId, videoId);
        String key = String.format("watch:%d:%d", userId, videoId);
        String watchTimeStr = redisTemplate.opsForValue().get(key);

        if (watchTimeStr != null) {
            int watchTime = Integer.parseInt(watchTimeStr);
            User user = userService.findUserById(userId);
            Video video = videoService.getVideoById(videoId);

            WatchHistory watchHistory = watchHistoryRepository.findByUserAndVideo(user, video)
                    .orElse(WatchHistory.builder()
                            .user(user)
                            .video(video)
                            .build());

            watchHistory.updateWatchHistoryTime(watchTime);
            watchHistoryRepository.save(watchHistory);

            log.info("Updated watch history in DB for user: {} and video: {} at time: {}", userId, videoId, watchTime);
        } else {
            log.warn("No watch history found in Redis for user: {} and video: {}", userId, videoId);
        }
    }

    @Transactional
    public void endWatching(Long userId, Long videoId) {
        log.info("Ending watch session for user: {} and video: {}", userId, videoId);
        String key = String.format("watch:%d:%d", userId, videoId);
        redisTemplate.delete(key);
        log.info("Watch session ended for user: {} and video: {}", userId, videoId);
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간 실행
    @Transactional
    public void syncRedisToDatabase() {
        log.info("Starting daily watch history sync from Redis to Database");
        Set<String> keys = redisTemplate.keys("watch:*");
        int count = 0;
        int totalKeys = keys.size();
        log.info("Found {} keys to sync", totalKeys);

        for (String key : keys) {
            String[] parts = key.split(":");
            Long userId = Long.parseLong(parts[1]);
            Long videoId = Long.parseLong(parts[2]);
            String watchTimeStr = redisTemplate.opsForValue().get(key);
            if (watchTimeStr != null) {
                int watchTime = Integer.parseInt(watchTimeStr);
                updateWatchHistoryInDatabase(userId, videoId, watchTime);
                count++;
                if (count % 100 == 0) {  // 로그 과다 방지를 위해 100개마다 로그 출력
                    log.info("Synced {} out of {} records", count, totalKeys);
                }
            } else {
                log.warn("No watch time found for key: {}", key);
            }
        }
        log.info("Completed daily watch history sync. Updated {} out of {} records", count, totalKeys);
    }

    private void updateWatchHistoryInDatabase(Long userId, Long videoId, int watchTime) {
        try {
            User user = userService.findUserById(userId);
            Video video = videoService.getVideoById(videoId);
            WatchHistory watchHistory = watchHistoryRepository.findByUserAndVideo(user, video)
                    .orElse(WatchHistory.builder()
                            .user(user)
                            .video(video)
                            .watchHistoryTime(0)
                            .build());
            watchHistory.updateWatchHistoryTime(watchTime);
            watchHistoryRepository.save(watchHistory);
            log.debug("Updated watch history in DB for user: {} and video: {} to time: {}", userId, videoId, watchTime);
        } catch (Exception e) {
            log.error("Error updating watch history in DB for user: {} and video: {}", userId, videoId, e);
        }
    }





    @Transactional(readOnly = true)
    public int countViewsByVideoAndDate(Long videoId, LocalDate date) {
        return watchHistoryRepository.countViewsByVideoAndDate(videoId, date);
    }

    @Transactional(readOnly = true)
    public long sumWatchTimeByVideoAndDate(Long videoId, LocalDate date) {
        return watchHistoryRepository.sumWatchTimeByVideoAndDate(videoId, date);
    }



    @Transactional(readOnly = true)
    public List<Long> findActiveVideoIdsByDate(LocalDate date) {
        return watchHistoryRepository.findActiveVideoIdsByDate(date);
    }

    @Transactional
    public WatchHistory save(WatchHistory watchHistory) {
        return watchHistoryRepository.save(watchHistory);
    }
}

