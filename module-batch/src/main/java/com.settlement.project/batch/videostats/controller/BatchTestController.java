package com.settlement.project.batch.videostats.controller;



import com.settlement.project.batch.videostats.scheduler.VideoStatsScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@RestController
@RequestMapping("/api/video-stats/test")
public class BatchTestController {
        @Autowired
    private VideoStatsScheduler videoStatsScheduler;
    @PostMapping("/test-scheduler")
    public ResponseEntity<String> testScheduler() throws Exception {
        // 성능 측정을 위한 시간 기록 시작
        Instant startTime = Instant.now();

        // CPU 사용량 측정 시작
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double startCpuLoad = osBean.getSystemLoadAverage();

        // VideoStats 배치 작업 실행
        videoStatsScheduler.runUpdateVideoStatsJob();

        // 성능 측정을 위한 시간 기록 종료
        Instant endTime = Instant.now();
        Duration timeElapsed = Duration.between(startTime, endTime);

        // CPU 사용량 측정 종료
        double endCpuLoad = osBean.getSystemLoadAverage();

        // 메모리 사용량 측정
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed() / (1024 * 1024); // MB로 변환

        // 마크다운 파일 생성 및 저장
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filePath = "docs/videoStats/videostats-performance-" + timestamp + ".md";

        StringBuilder markdown = new StringBuilder();
        markdown.append("### VideoStats 배치 작업 성능 테스트 결과\n\n");
        markdown.append("| 작업명           | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |\n");
        markdown.append("|------------------|----------------|--------------------|--------------------|-------------------|\n");
        markdown.append(String.format("| %s | %.2f초 | %.2f%% | %.2f%% | %dMB |\n",
                "VideoStatsBatchJob", timeElapsed.toMillis() / 1000.0, startCpuLoad * 100, endCpuLoad * 100, usedMemory));

        saveMarkdownToFile(filePath, markdown.toString());

        return ResponseEntity.ok("Scheduler test completed successfully, and performance data has been saved.");
    }

    // 마크다운 파일에 저장하는 메서드
    private void saveMarkdownToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            System.out.println("파일이 성공적으로 저장되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello, Batch Test!");
    }

}
