package com.settlement.project.main.dummyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class DummyDataController {

    @Autowired
    private DummyDataGeneratorService dummyDataGeneratorService;

    @Autowired
    private AdDummyDataGeneratorService adDummyDataGeneratorService;

    @PostMapping("/generate-dummy-data")
    public String generateDummyData() {
        return dummyDataGeneratorService.generateDummyDataAndMeasurePerformance();
    }

    @GetMapping("/generateAds")
    public String generateDummyAds() {
        // 서비스 호출하여 더미 데이터를 생성
        return adDummyDataGeneratorService.generateDummyAds();
    }
}