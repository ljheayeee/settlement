package com.settlement.project.main.controller;

import com.settlement.project.main.config.DummyDataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class DummyDataController {

    @Autowired
    private DummyDataGeneratorService dummyDataGeneratorService;

    @PostMapping("/generate-dummy-data")
    public String generateDummyData() {
        return dummyDataGeneratorService.generateDummyDataAndMeasurePerformance();
    }
}