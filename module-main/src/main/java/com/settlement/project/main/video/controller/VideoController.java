package com.settlement.project.main.video.controller;

import com.settlement.project.common.user.entity.UserRoleEnum;
import com.settlement.project.common.video.dto.VideoRequestDto;
import com.settlement.project.common.video.dto.VideoResponseDto;
import com.settlement.project.common.video.dto.VideoStatusRequestDto;
import com.settlement.project.main.video.service.VideoService;
import com.settlement.project.main.user.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/v1/videos")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
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
    public ResponseEntity<VideoResponseDto> createVideo(@RequestBody VideoRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails.getUser().getRole() != UserRoleEnum.SELLER) {
            throw new AccessDeniedException("Only sellers can upload videos");
        }

        Long userId = userDetails.getUserId();

        VideoRequestDto updatedRequestDto = requestDto.toBuilder()
                .userId(userId)
                .build();

        VideoResponseDto responseDto = videoService.createVideo(updatedRequestDto);

        return ResponseEntity.ok(responseDto);
    }

    /** 비디오 가져오기 **/
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDto> getVideoById(@PathVariable Long id) {
        VideoResponseDto videoDto = VideoResponseDto.fromEntity(videoService.getVideoById(id));
        return ResponseEntity.ok(videoDto);
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
