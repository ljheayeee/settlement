package com.settlement.video.controller;

import com.settlement.video.dto.VideoRequestDto;
import com.settlement.video.dto.VideoResponseDto;
import com.settlement.video.entity.User;
import com.settlement.video.entity.Video;
import com.settlement.video.service.UserService;
import com.settlement.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;
    private final UserService userService;



    public VideoController(VideoService videoService, UserService userService) {
        this.videoService = videoService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoResponseDto> createVideo(@RequestBody VideoRequestDto requestDto) {
        VideoResponseDto responseDto = videoService.createVideo(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Video> updateVideo(@PathVariable Long id, @RequestBody Video videoDetails) {
        return ResponseEntity.ok(videoService.updateVideo(id, videoDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/{id}/view")
//    public ResponseEntity<?> incrementView(@PathVariable Long id) {
//        videoService.incrementView(id);
//
//        // 비디오 등록
//    // 비디오 수정
//    // 비디오 비활성화
//    // 비디오 삭제
//        return null;
//    }

}
