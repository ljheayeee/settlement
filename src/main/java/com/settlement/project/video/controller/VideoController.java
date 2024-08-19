package com.settlement.project.video.controller;

import com.settlement.project.video.dto.StreamingResponseDto;
import com.settlement.project.video.dto.VideoRequestDto;
import com.settlement.project.video.dto.VideoResponseDto;
import com.settlement.project.video.dto.VideoStatusRequestDto;
import com.settlement.project.ads.entity.Ad;
import com.settlement.project.video.entity.Video;
import com.settlement.project.stats.service.StatsService;
import com.settlement.project.user.service.UserDetailsImpl;
import com.settlement.project.video.service.VideoService;
import com.settlement.project.watchhisotry.service.WatchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/v1/videos")
public class VideoController {
    private final VideoService videoService;
    private final StatsService statsService;

    private final WatchHistoryService watchHistoryService;

    public VideoController(VideoService videoService, StatsService statsService, WatchHistoryService watchHistoryService) {
        this.videoService = videoService;
        this.statsService = statsService;
        this.watchHistoryService = watchHistoryService;
    }
    @GetMapping
    public ResponseEntity<Page<VideoResponseDto>> getAllActiveVideos(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<VideoResponseDto> videos = videoService.getAllActiveVideos(keyword, pageable);
        return ResponseEntity.ok(videos);
    }

    /** 동영상 등록**/
    @PostMapping("/upload")
    public ResponseEntity<VideoResponseDto> createVideo(@RequestBody VideoRequestDto requestDto) {
        VideoResponseDto responseDto = videoService.createVideo(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /** 비디오 가져오기 **/
    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }
    /** 비디오 수정 **/
    @PutMapping("/{id}")
    public ResponseEntity<VideoResponseDto> updateVideo(@PathVariable Long id,
                                                        @RequestBody VideoRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();
        VideoResponseDto updatedVideo = videoService.updateVideo(id, requestDto, userId);
        return ResponseEntity.ok(updatedVideo);
    }
    /** 비디오 시작 **/
    @PostMapping("/{id}/play")
    public ResponseEntity<Void> startPlaying(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        watchHistoryService.startWatching(userDetails.getUserId(), id);
        return ResponseEntity.ok().build();
    }
    /** 비디오 시작 **/
    @PutMapping("/{id}/watchHistoryTime")
    public ResponseEntity<Void> updateWatchHistoryTime(@PathVariable Long id,
                                                       @RequestParam int watchHistoryTime,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        watchHistoryService.updateWatchHistoryTime(userDetails.getUserId(), id, watchHistoryTime);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pausePlaying(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        watchHistoryService.pauseWatching(userDetails.getUserId(), id);
        return ResponseEntity.ok().build();
    }


//    @GetMapping("/{id}/ads")
//    public ResponseEntity<List<Ad>> getAdsForVideo(@PathVariable Long id,
//                                                   VideoRequestDto requestDto,
//                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        List<Ad> ads = statsService.getAdsForVideo(id, userDetails.getUserId());
//        return ResponseEntity.ok(ads);
//    }

    @PutMapping("/{id}/status")
    public ResponseEntity<VideoResponseDto> updateVideoStatus(
            @PathVariable Long id,
            @RequestBody VideoStatusRequestDto statusUpdateDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        VideoResponseDto statusUpdatedVideo = videoService.updateVideoStatus(id, statusUpdateDto.getStatus(), userDetails.getUserId());
        return ResponseEntity.ok(statusUpdatedVideo);
    }


    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        videoService.deleteVideo(videoId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }


}
