package com.settlement.project.revenues.config;

import com.settlement.project.revenues.dto.RevenueCalculationRequestDto;
import com.settlement.project.revenues.service.RevenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
public class RevenueCalculationBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(RevenueCalculationBatchConfig.class);
    private final RevenueService revenueService;

    public RevenueCalculationBatchConfig(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @Bean
    public Job revenueCalculationJob(JobRepository jobRepository, Step revenueCalculationStep) {
        return new JobBuilder("revenueCalculationJob", jobRepository)
                .start(revenueCalculationStep)
                .build();
    }

    @Bean
    public Step revenueCalculationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("revenueCalculationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate calculationDate = LocalDate.now().minusDays(1);
                    logger.info("Starting revenue calculation for date: {}", calculationDate);
                    try {
                        RevenueCalculationRequestDto requestDto = RevenueCalculationRequestDto.builder()
                                .calculationDate(calculationDate)
                                .build();
                        revenueService.calculateDailyRevenue(requestDto);
                        logger.info("Completed revenue calculation for date: {}", calculationDate);
                    } catch (Exception e) {
                        logger.error("Error occurred during revenue calculation for date: {}", calculationDate, e);
                        throw e;
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}