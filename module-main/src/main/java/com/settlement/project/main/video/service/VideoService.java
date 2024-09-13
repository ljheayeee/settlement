package com.settlement.project.main.video.service;

import com.settlement.project.main.ads.service.AdService;
import com.settlement.project.common.user.entity.User;
import com.settlement.project.main.user.service.UserService;
import com.settlement.project.main.video.exception.VideoCreationException;
import com.settlement.project.common.video.dto.VideoRequestDto;
import com.settlement.project.common.video.dto.VideoResponseDto;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.entity.VideoStatusEnum;
import com.settlement.project.common.video.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private final VideoRepository videoRepository;
    private final UserService userService;
    private final AdService adService;
    private static final int AD_INTERVAL_SECONDS = 300; // 5분
    private static final int BATCH_SIZE = 1000; // 한 번에 처리할 비디오 ID 수


    public VideoService(VideoRepository videoRepository, UserService userService,
                        AdService adService) {
        this.videoRepository = videoRepository;
        this.userService = userService;
        this.adService = adService;
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

    @Cacheable(value = "activeVideos", key = "#keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
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
    public void incrementViewCount(Long videoId) {
        Video video = getVideoById(videoId);
        video.incrementViewCount();
        videoRepository.save(video);
        log.info("Incremented view count for video: {}", videoId);
    }




    public List<Long> getAllVideoIds() {
        long totalVideos = videoRepository.countAllVideos();
        List<Long> allVideoIds = new ArrayList<>();

        for (int offset = 0; offset < totalVideos; offset += BATCH_SIZE) {
            allVideoIds.addAll(videoRepository.findVideoIdsPaginated(offset, BATCH_SIZE));
        }

        return allVideoIds;
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Video> findByStatus(VideoStatusEnum status, Pageable pageable) {
        return videoRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Video> findByStatusAndTitleContainingIgnoreCase(VideoStatusEnum status, String keyword, Pageable pageable) {
        return videoRepository.findByStatusAndTitleContainingIgnoreCase(status, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<Long> findAllVideoIds() {
        return videoRepository.findAllVideoIds();
    }

    @Transactional(readOnly = true)
    public List<Long> findVideoIdsPaginated(int offset, int limit) {
        return videoRepository.findVideoIdsPaginated(offset, limit);
    }

    @Transactional(readOnly = true)
    public long countAllVideos() {
        return videoRepository.countAllVideos();
    }

    @Transactional
    public Video save(Video video) {
        return videoRepository.save(video);
    }

    @Transactional
    public void delete(Video video) {
        videoRepository.delete(video);
    }



    public Map<Long, String> getVideoTitles(Set<Long> videoIds) {
        List<Video> videos = videoRepository.findAllById(videoIds);
        return videos.stream().collect(Collectors.toMap(Video::getId, Video::getTitle));
    }


}