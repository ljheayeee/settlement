package com.settlement.project.main.revenues.controller;


import com.settlement.project.common.revenues.dto.UserRevenueDetailResponseDto;
import com.settlement.project.main.revenues.service.RevenueService;
import com.settlement.project.main.user.service.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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