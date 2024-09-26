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


}