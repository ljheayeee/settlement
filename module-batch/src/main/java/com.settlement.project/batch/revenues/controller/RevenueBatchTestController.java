package com.settlement.project.batch.revenues.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
public class RevenueBatchTestController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job revenueCalculationJob;

    @PostMapping("/test-calculation")
    public ResponseEntity<String> testCalculation() {
        // 성능 측정을 위한 시간 기록 시작
        Instant startTime = Instant.now();

        // CPU 사용량 측정 시작
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double startCpuLoad = osBean.getSystemLoadAverage();

        // 배치 잡 실행
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())  // 'time'은 고유한 실행을 보장하기 위해 사용
                    .toJobParameters();

            jobLauncher.run(revenueCalculationJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Batch Job Failed: " + e.getMessage());
        }

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
        savePerformanceMarkdown(timeElapsed, startCpuLoad, endCpuLoad, usedMemory);

        return ResponseEntity.ok("Batch Job Completed Successfully, performance data has been saved.");
    }

    // 마크다운 파일 생성 및 저장하는 메서드
    private void savePerformanceMarkdown(Duration timeElapsed, double startCpuLoad, double endCpuLoad, long usedMemory) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filePath = "docs/revenue/revenue-performance-" + timestamp + ".md";

        StringBuilder markdown = new StringBuilder();
        markdown.append("### Revenue 배치 작업 성능 테스트 결과\n\n");
        markdown.append("| 작업명           | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |\n");
        markdown.append("|------------------|----------------|--------------------|--------------------|-------------------|\n");
        markdown.append(String.format("| %s | %.2f초 | %.2f%% | %.2f%% | %dMB |\n",
                "RevenueBatchJob", timeElapsed.toMillis() / 1000.0, startCpuLoad * 100, endCpuLoad * 100, usedMemory));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(markdown.toString());
            System.out.println("파일이 성공적으로 저장되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
