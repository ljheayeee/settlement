package com.settlement.project.common.util;
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

public class BatchPerformanceTest {

    public static void main(String[] args) {
        // 현재 시간 가져오기
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // 각각의 배치 작업을 실행하고, 해당 디렉토리에 결과 저장
        executeAndSaveBatchJob("RevenueBatchJob", "docs/revenue/", timestamp);
        executeAndSaveBatchJob("VideoStatsBatchJob", "docs/videoStats/", timestamp);
    }

    // 배치 작업 실행 및 결과 저장 메서드
    private static void executeAndSaveBatchJob(String jobName, String outputDir, String timestamp) {
        // 파일명 생성 (시간을 포함한 파일명 생성)
        String outputFile = jobName.toLowerCase() + "-performance-" + timestamp + ".md";
        String filePath = outputDir + outputFile;

        // 마크다운 파일 작성
        StringBuilder markdown = new StringBuilder();
        markdown.append("### 배치 작업 성능 테스트 결과\n\n");
        markdown.append("| 작업명              | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |\n");
        markdown.append("|---------------------|----------------|--------------------|--------------------|-------------------|\n");

        // 배치 작업 실행 및 성능 측정
        markdown.append(executeAndMeasureBatchJob(jobName));

        // 마크다운 파일에 저장
        saveMarkdownToFile(filePath, markdown.toString());
    }

    // 배치 작업 실행 및 성능 측정을 하는 메서드
    private static String executeAndMeasureBatchJob(String jobName) {
        // 측정 시작
        Instant startTime = Instant.now();

        // CPU 사용량 측정 시작
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double startCpuLoad = osBean.getSystemLoadAverage();

        // 배치 작업 실행 (여기에 실제 배치 작업 메서드를 호출)
        executeBatchJob(jobName);

        // 측정 종료
        Instant endTime = Instant.now();
        Duration timeElapsed = Duration.between(startTime, endTime);

        // CPU 사용량 측정 종료
        double endCpuLoad = osBean.getSystemLoadAverage();

        // 메모리 사용량 측정
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed() / (1024 * 1024); // MB로 변환

        // 결과를 마크다운 형식으로 반환
        return String.format("| %s | %.2f초 | %.2f%% | %.2f%% | %dMB |\n",
                jobName, timeElapsed.toMillis() / 1000.0, startCpuLoad * 100, endCpuLoad * 100, usedMemory);
    }

    // 실제 배치 작업을 실행하는 메서드
    private static void executeBatchJob(String jobName) {
        // 실제 배치 작업 로직 추가
        System.out.println(jobName + " 실행 중...");
        try {
            // 각 배치 작업마다 다른 로직을 수행할 수 있음
            Thread.sleep(2000); // 테스트를 위해 2초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 마크다운 파일에 저장하는 메서드
    private static void saveMarkdownToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            System.out.println("파일이 성공적으로 저장되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
