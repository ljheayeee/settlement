package com.settlement.project.batch.videostats.config;

import com.settlement.project.batch.videostats.service.VideoStatsBatchService;

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
    private final VideoStatsBatchService videoStatsBatchService;

    public VideoStatsBatchConfig(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 VideoStatsBatchService videoStatsBatchService) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.videoStatsBatchService = videoStatsBatchService;
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
//                    LocalDate yesterday = LocalDate.now().minusDays(1);
//                    videoStatsService.updateDailyStats(yesterday);
                    // 임시 수정: 오늘의 데이터를 처리
                    LocalDate today = LocalDate.now();  // 오늘의 날짜 사용
                    videoStatsBatchService.updateDailyStats(today);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}