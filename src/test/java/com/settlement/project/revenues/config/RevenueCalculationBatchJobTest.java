package com.settlement.project.revenues.config;

import com.settlement.project.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.revenues.service.RevenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBatchTest
@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ContextConfiguration(classes = {RevenueCalculationBatchConfig.class})
public class RevenueCalculationBatchJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockBean
    private RevenueService revenueService;

    @Test
    @DisplayName("수익 계산 배치 작업 테스트")
    public void testRevenueCalculationBatchJob() throws Exception {
        // Given
        doNothing().when(revenueService).calculateDailyRevenue(any(RevenueCalculationRequestDto.class));

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // Then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        verify(revenueService, times(1)).calculateDailyRevenue(any(RevenueCalculationRequestDto.class));
    }
}