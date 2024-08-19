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
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private final VideoRepository videoRepository;
    private final UserService userService;
    private final AdService adService;
    private final StatsService statsService;
    private static final int AD_INTERVAL_SECONDS = 300; // 5분

    public VideoService(VideoRepository videoRepository, UserService userService,
                        AdService adService, StatsService statsService) {
        this.videoRepository = videoRepository;
        this.userService = userService;
        this.adService = adService;
        this.statsService = statsService;
    }

    //동영상 등록
    @Transactional
    public VideoResponseDto createVideo(VideoRequestDto requestDto) {
        try {
            User user = userService.findUserById(requestDto.getUserId());
            Video video = videoRepository.save(requestDto.toEntity(user));
            adService.assignAdsToVideo(video.getId(), video.getPlayTimeInMinutes());
            log.info("New video created: {}", video.getId());
            return VideoResponseDto.fromEntity(video);
        } catch (Exception e) {
            log.error("Error creating video: ", e);
            throw new VideoCreationException("Failed to create video", e);
        }
    }


    public VideoResponseDto updateVideoStatus(Long videoId, VideoStatusEnum newStatus, Long userId) {
        Video video = findVideoAndCheckPermission(videoId, userId);
        video.updateStatus(newStatus);
        return VideoResponseDto.fromEntity(video);
    }

    @Cacheable(value = "activeVideos", key = "#keyword + '_' + #pageable.pageNumber")
    public Page<VideoResponseDto> getAllActiveVideos(String keyword, Pageable pageable) {
        log.debug("Fetching active videos with keyword: {}, page: {}", keyword, pageable.getPageNumber());
        Page<Video> videoPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            videoPage = videoRepository.findByStatusAndTitleContainingIgnoreCase(
                    VideoStatusEnum.ACTIVATE, keyword.trim(), pageable);
        } else {
            videoPage = videoRepository.findByStatus(VideoStatusEnum.ACTIVATE, pageable);
        }
        return videoPage.map(VideoResponseDto::fromEntity);
    }


    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
    }

    @Transactional
    public VideoResponseDto updateVideo(Long videoId, VideoRequestDto requestDto, Long userId) {
        Video video = findVideoAndCheckPermission(videoId, userId);
        video.update(requestDto);
        return VideoResponseDto.fromEntity(video);
    }

    @Transactional
    public void deleteVideo(Long videoId, Long userId) {
        Video video = findVideoAndCheckPermission(videoId, userId);
        videoRepository.delete(video);
    }

    private Video findVideoAndCheckPermission(Long videoId, Long userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + videoId));

        if (!video.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to modify this video");
        }

        return video;
    }


    @Transactional
    public void checkAndPlayAd(Long videoId, int watchHistoryTime) {
        try {
            int adIndex = watchHistoryTime / AD_INTERVAL_SECONDS;
            List<Long> adIds = statsService.getAdIdsForVideo(videoId);

            if (watchHistoryTime % AD_INTERVAL_SECONDS == 0 && adIndex > 0 && adIndex <= adIds.size()) {
                Long adId = adIds.get(adIndex - 1);
                statsService.incrementAdViewCount(videoId, adId);
                log.info("Ad played for video: {}, ad: {}", videoId, adId);
            }
        } catch (Exception e) {
            log.error("Error checking and playing ad for video: {}", videoId, e);
            throw new AdPlaybackException("Failed to check and play ad", e);
        }
    }




}