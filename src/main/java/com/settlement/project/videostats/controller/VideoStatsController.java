package com.settlement.project.videostats.controller;

import com.settlement.project.user.service.UserDetailsImpl;
import com.settlement.project.videostats.dto.TopVideosByViewsResponseDto;
import com.settlement.project.videostats.dto.TopVideosByWatchTimeResponseDto;
import com.settlement.project.videostats.dto.VideoStatsResponseDto;
import com.settlement.project.videostats.scheduler.VideoStatsScheduler;
import com.settlement.project.videostats.service.VideoStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/video-stats")
@Slf4j
public class VideoStatsController {
    @Autowired
    private VideoStatsScheduler videoStatsScheduler;
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


    @PostMapping("/test-scheduler")
    public ResponseEntity<String> testScheduler() throws Exception {
        videoStatsScheduler.runUpdateVideoStatsJob();
        return ResponseEntity.ok("Scheduler test completed successfully");
    }
}