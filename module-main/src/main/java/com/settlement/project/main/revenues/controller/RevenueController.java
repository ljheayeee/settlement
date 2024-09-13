package com.settlement.project.main.revenues.controller;


import com.settlement.project.common.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.common.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.main.revenues.service.RevenueService;
import com.settlement.project.common.revenues.entity.Revenue;
import com.settlement.project.main.user.service.UserDetailsImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
public class RevenueController {
    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/{period}")
    public ResponseEntity<UserRevenueDetailResponseDto> getUserRevenueDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String period) {
        UserRevenueDetailResponseDto result = revenueService.getUserRevenueDetail(userDetails.getUserId(), period);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-calculation/{date}")
    public ResponseEntity<List<Revenue>> testCalculation(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // 성능 측정을 위한 시간 기록 시작
        Instant startTime = Instant.now();

        // CPU 사용량 측정 시작
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double startCpuLoad = osBean.getSystemLoadAverage();

        // 기존 로직: requestDto 생성 후 수익 계산 실행
        RevenueCalculationRequestDto requestDto = RevenueCalculationRequestDto.builder()
                .calculationDate(date)
                .build();
        revenueService.calculateDailyRevenue(requestDto);

        List<Revenue> calculatedRevenues = revenueService.getRevenuesByDate(date);

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
        savePerformanceMarkdown(date, timeElapsed, startCpuLoad, endCpuLoad, usedMemory);

        return ResponseEntity.ok(calculatedRevenues);
    }

    // 마크다운 파일 생성 및 저장하는 메서드
    private void savePerformanceMarkdown(LocalDate date, Duration timeElapsed, double startCpuLoad, double endCpuLoad, long usedMemory) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filePath = "docs/revenue/revenue-performance-" + timestamp + ".md";

        StringBuilder markdown = new StringBuilder();
        markdown.append("### Revenue 배치 작업 성능 테스트 결과\n\n");
        markdown.append("| 작업명           | 실행 날짜       | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |\n");
        markdown.append("|------------------|-----------------|----------------|--------------------|--------------------|-------------------|\n");
        markdown.append(String.format("| %s | %s | %.2f초 | %.2f%% | %.2f%% | %dMB |\n",
                "RevenueBatchJob", date, timeElapsed.toMillis() / 1000.0, startCpuLoad * 100, endCpuLoad * 100, usedMemory));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(markdown.toString());
            System.out.println("파일이 성공적으로 저장되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}