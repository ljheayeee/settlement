package com.settlement.project.revenues.scheduler;

import com.settlement.project.revenues.scheduler.RevenueCalculationScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class RevenueCalculationSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job revenueCalculationJob;

    @InjectMocks
    private RevenueCalculationScheduler scheduler;

    @Test
    @DisplayName("수익 계산 스케줄러 테스트")
    void testRevenueCalculationScheduler() throws Exception {
        // Given
        when(jobLauncher.run(eq(revenueCalculationJob), any(JobParameters.class)))
                .thenReturn(new JobExecution(1L));

        // When
        scheduler.runDailyRevenueCalculation();

        // Then
        verify(jobLauncher, times(1)).run(eq(revenueCalculationJob), any(JobParameters.class));
    }
}
