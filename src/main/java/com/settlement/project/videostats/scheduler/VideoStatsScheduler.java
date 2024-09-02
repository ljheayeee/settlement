package com.settlement.project.videostats.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoStatsScheduler {

    private final JobLauncher jobLauncher;
    private final Job updateVideoStatsJob;

    public VideoStatsScheduler(JobLauncher jobLauncher, Job updateVideoStatsJob) {
        this.jobLauncher = jobLauncher;
        this.updateVideoStatsJob = updateVideoStatsJob;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정(00:00)에 실행
    public void runUpdateVideoStatsJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(updateVideoStatsJob, jobParameters);
    }
}