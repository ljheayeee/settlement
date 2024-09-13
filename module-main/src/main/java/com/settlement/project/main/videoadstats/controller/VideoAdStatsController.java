package com.settlement.project.main.videoadstats.controller;

import com.settlement.project.main.videoadstats.dto.VideoAdStatsRequestDto;
import com.settlement.project.main.videoadstats.dto.VideoAdStatsResponseDto;
import com.settlement.project.main.videoadstats.service.VideoAdStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-ad-stats")
public class VideoAdStatsController {
    private final VideoAdStatsService videoAdStatsService;

    public VideoAdStatsController(VideoAdStatsService videoAdStatsService) {
        this.videoAdStatsService = videoAdStatsService;
    }

    @PostMapping
    public ResponseEntity<VideoAdStatsResponseDto> createOrUpdateStats(@RequestBody VideoAdStatsRequestDto requestDto) {
        VideoAdStatsResponseDto responseDto = videoAdStatsService.createOrUpdateStats(
                requestDto.getVideoId(), requestDto.getAdId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<VideoAdStatsResponseDto>> getStatsForVideo(@PathVariable Long videoId) {
        List<VideoAdStatsResponseDto> stats = videoAdStatsService.getStatsForVideo(videoId);
        return ResponseEntity.ok(stats);
    }
}