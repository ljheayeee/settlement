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
//    @Autowired
//    private VideoStatsScheduler videoStatsScheduler;
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


//    @PostMapping("/test-scheduler")
//    public ResponseEntity<String> testScheduler() throws Exception {
//        // 성능 측정을 위한 시간 기록 시작
//        Instant startTime = Instant.now();
//
//        // CPU 사용량 측정 시작
//        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
//        double startCpuLoad = osBean.getSystemLoadAverage();
//
//        // VideoStats 배치 작업 실행
//        videoStatsScheduler.runUpdateVideoStatsJob();
//
//        // 성능 측정을 위한 시간 기록 종료
//        Instant endTime = Instant.now();
//        Duration timeElapsed = Duration.between(startTime, endTime);
//
//        // CPU 사용량 측정 종료
//        double endCpuLoad = osBean.getSystemLoadAverage();
//
//        // 메모리 사용량 측정
//        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
//        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
//        long usedMemory = heapUsage.getUsed() / (1024 * 1024); // MB로 변환
//
//        // 마크다운 파일 생성 및 저장
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
//        String filePath = "docs/videoStats/videostats-performance-" + timestamp + ".md";
//
//        StringBuilder markdown = new StringBuilder();
//        markdown.append("### VideoStats 배치 작업 성능 테스트 결과\n\n");
//        markdown.append("| 작업명           | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |\n");
//        markdown.append("|------------------|----------------|--------------------|--------------------|-------------------|\n");
//        markdown.append(String.format("| %s | %.2f초 | %.2f%% | %.2f%% | %dMB |\n",
//                "VideoStatsBatchJob", timeElapsed.toMillis() / 1000.0, startCpuLoad * 100, endCpuLoad * 100, usedMemory));
//
//        saveMarkdownToFile(filePath, markdown.toString());
//
//        return ResponseEntity.ok("Scheduler test completed successfully, and performance data has been saved.");
//    }
//
//    // 마크다운 파일에 저장하는 메서드
//    private void saveMarkdownToFile(String filePath, String content) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            writer.write(content);
//            System.out.println("파일이 성공적으로 저장되었습니다: " + filePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}