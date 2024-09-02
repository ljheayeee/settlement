package com.settlement.project.revenues.controller;

import com.settlement.project.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.revenues.entity.Revenue;
import com.settlement.project.revenues.service.RevenueService;
import com.settlement.project.user.service.UserDetailsImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        RevenueCalculationRequestDto requestDto = RevenueCalculationRequestDto.builder()
                .calculationDate(date)
                .build();
        revenueService.calculateDailyRevenue(requestDto);
        List<Revenue> calculatedRevenues = revenueService.getRevenuesByDate(date);
        return ResponseEntity.ok(calculatedRevenues);
    }
}