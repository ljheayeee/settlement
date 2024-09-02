package com.settlement.project.watchhisotry.controller;

import com.settlement.project.videoadstats.dto.PlayAdResponseDto;
import com.settlement.project.user.service.UserDetailsImpl;
import com.settlement.project.watchhisotry.dto.StartWatchingRequestDto;
import com.settlement.project.watchhisotry.dto.WatchHistoryRequestDto;
import com.settlement.project.watchhisotry.service.WatchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watch-history")
@Slf4j(topic = "시청기록 컨트롤러")
public class WatchHistoryController {
    private final WatchHistoryService watchHistoryService;

    public WatchHistoryController(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }

@PostMapping("/start")
public ResponseEntity<Integer> startWatching(@RequestBody StartWatchingRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
    log.info("startWatching called. UserDetails: {}", userDetails);
    log.info("UserID: {}", userDetails.getUserId());
    int lastPosition = watchHistoryService.startWatching(requestDto.getVideoId(), userDetails.getUserId());
    log.info("Last position: {}", lastPosition);
    return ResponseEntity.ok(lastPosition);
}
    @PutMapping("/update")
    public ResponseEntity<PlayAdResponseDto> updateWatchHistoryTime(@RequestBody WatchHistoryRequestDto requestDto,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PlayAdResponseDto adResponse = watchHistoryService.updateWatchHistoryTime(
                userDetails.getUserId(), requestDto.getVideoId(), requestDto.getWatchHistoryTime());

        if (adResponse != null) {
            return ResponseEntity.ok(adResponse);  // 광고가 재생된 경우 광고 정보를 반환
        } else {
            return ResponseEntity.ok().build();  // 광고가 재생되지 않은 경우 200 OK 응답만 반환
        }
    }


    @PostMapping("/pause")
    public ResponseEntity<Void> pauseWatching(@RequestBody WatchHistoryRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        watchHistoryService.pauseWatching(userDetails.getUserId(), requestDto.getVideoId());
        return ResponseEntity.ok().build();
    }
}

