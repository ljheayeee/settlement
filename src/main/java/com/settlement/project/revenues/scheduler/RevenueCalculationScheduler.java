package com.settlement.project.revenues.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RevenueCalculationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RevenueCalculationScheduler.class);
    private final JobLauncher jobLauncher;
    private final Job revenueCalculationJob;

    public RevenueCalculationScheduler(JobLauncher jobLauncher, Job revenueCalculationJob) {
        this.jobLauncher = jobLauncher;
        this.revenueCalculationJob = revenueCalculationJob;
    }

    @Scheduled(cron = "0 30 0 * * ?") // 매일 00:30에 실행
    public void runDailyRevenueCalculation() {
        logger.info("Starting daily revenue calculation job");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(revenueCalculationJob, jobParameters);
            logger.info("Completed daily revenue calculation job");
        } catch (Exception e) {
            logger.error("Error occurred during daily revenue calculation job", e);
        }
    }
}