package com.settlement.project.videostats.config;

import com.settlement.project.videostats.service.VideoStatsService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
public class VideoStatsBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final VideoStatsService videoStatsService;

    public VideoStatsBatchConfig(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 VideoStatsService videoStatsService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.videoStatsService = videoStatsService;
    }

    @Bean
    public Job updateVideoStatsJob() {
        return new JobBuilder("updateVideoStatsJob", jobRepository)
                .start(updateDailyStatsStep())
                .build();
    }

    @Bean
    public Step updateDailyStatsStep() {
        return new StepBuilder("updateDailyStatsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    videoStatsService.updateDailyStats(yesterday);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}