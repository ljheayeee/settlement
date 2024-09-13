package com.settlement.project.main.ads.controller;

import com.settlement.project.common.ads.dto.AdRequestDto;
import com.settlement.project.common.ads.dto.AdResponseDto;
import com.settlement.project.main.ads.service.AdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ads")
public class AdController {
    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @PostMapping
    public ResponseEntity<AdResponseDto> createAd(@RequestBody AdRequestDto requestDto) {
        AdResponseDto responseDto = adService.createAd(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdResponseDto> getAdById(@PathVariable Long id) {
        AdResponseDto adDto = AdResponseDto.fromEntity(adService.getAdById(id));
        return ResponseEntity.ok(adDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdResponseDto> updateAd(@PathVariable Long id, @RequestBody AdRequestDto requestDto) {
        AdResponseDto updatedAdDto = adService.updateAd(id, requestDto);
        return ResponseEntity.ok(updatedAdDto);
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<String> deleteAd(@PathVariable Long id) {
        adService.softDeleteAd(id);
        return ResponseEntity.ok("Ad deleted successfully");
    }

    //임시 스케줄러 테스트
    @PostMapping("/update-statuses")
    public ResponseEntity<String> updateAdStatuses() {
        adService.updateAdStatuses();
        return ResponseEntity.ok("Ad statuses updated successfully");
    }


}

