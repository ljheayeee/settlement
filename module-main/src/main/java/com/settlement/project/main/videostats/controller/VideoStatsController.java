package com.settlement.project.main.videostats.controller;

import com.settlement.project.main.user.service.UserDetailsImpl;
import com.settlement.project.main.videostats.service.VideoStatsService;
import com.settlement.project.common.videostats.dto.TopVideosByViewsResponseDto;
import com.settlement.project.common.videostats.dto.TopVideosByWatchTimeResponseDto;
import com.settlement.project.common.videostats.dto.VideoStatsResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-stats")
@Slf4j
public class VideoStatsController {

    private final VideoStatsService videoStatsService;

    public VideoStatsController(VideoStatsService videoStatsService) {
        this.videoStatsService = videoStatsService;
    }

    @GetMapping("/top-views")
    public ResponseEntity<List<TopVideosByViewsResponseDto>> getTopVideosByViews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String period) {
        Long userId = userDetails.getUserId();
        List<TopVideosByViewsResponseDto> result = videoStatsService.getTop5VideosByViews(userId, period);
        log.debug("Controller result: {}", result);  //
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-watchtime")
    public ResponseEntity<List<TopVideosByWatchTimeResponseDto>> getTopVideosByWatchTime(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String period) {
        Long userId = userDetails.getUserId();
        List<TopVideosByWatchTimeResponseDto> result = videoStatsService.getTop5VideosByWatchTime(userId, period);
        log.debug("Controller result: {}", result);  //
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoStatsResponseDto> getVideoStats(
            @PathVariable Long videoId,
            @RequestParam String period) {
        VideoStatsResponseDto result = videoStatsService.getVideoStats(videoId, period);
        return ResponseEntity.ok(result);
    }



}